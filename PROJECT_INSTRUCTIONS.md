# Just Local - Project Instructions & Flow

## Overview
**Just Local** is an Android e-commerce application for local businesses. It supports four user roles: **Admin**, **Seller**, **Customer**, and **CSR (Customer Support Representative)**. The app is built with Java, uses Firebase (Auth, Realtime Database, Storage, Cloud Messaging) as the backend, and integrates TensorFlow Lite for AI-based product scanning.

---

## Tech Stack
| Component        | Technology                                      |
|------------------|------------------------------------------------|
| Language         | Java                                            |
| Backend          | Firebase (Auth, Realtime DB, Storage, FCM)      |
| AI/ML            | TensorFlow Lite (MobileNetV2 embeddings)        |
| Camera           | CameraX                                         |
| Image Loading    | Glide, Picasso                                  |
| UI               | ViewBinding, RecyclerView, Material Components   |
| Networking       | OkHttp                                          |
| PDF Generation   | iTextG                                          |
| Notifications    | Firebase Cloud Messaging (FCM)                   |

---

## Project Structure

```
com.example.justlocal/
├── LoginActivity.java                 # Entry point (LAUNCHER)
├── RegisterActivity.java              # User registration
├── ForgotPasswordActivity.java        # Password reset
├── MainActivity.java                  # Main activity
├── SettingsActivity.java              # App settings
│
├── AdminClass/
│   ├── AdminDashboardActivity.java         # Admin home
│   ├── UserManagementActivity.java        # Manage users
│   ├── EditUserActivity.java              # Edit user profile
│   ├── AdminProductManagementActivity.java # Manage products (approve/reject)
│   ├── EditAdminProduct.java              # Edit product details
│   ├── AdminOrderMonitoringActivity.java   # Monitor all orders
│   ├── AdminPaymentMonitoringActivity.java # Track payments/revenue
│   ├── AdminComplaintsActivity.java        # View all complaints
│   ├── AdminComplaintConversationActivity.java # View complaint thread
│   └── AdminGenerateReportActivity.java    # Generate PDF reports
│
├── SellerClass/
│   ├── SellerDashboardActivity.java       # Seller home
│   ├── AddProductActivity.java            # Add new product
│   ├── ProductManagementActivity.java     # Manage own products
│   ├── EditProductActivity.java           # Edit own product
│   ├── OrderMonitoringActivity.java       # View orders for own products
│   └── OrderDetailsActivity.java          # View order details
│
├── CustomerClass/
│   ├── CustomerDashboardActivity.java     # Customer home
│   ├── BrowseProductsActivity.java        # Browse & search products
│   ├── ViewProductsActivity.java          # Product detail view
│   ├── ProductAiScannerActivity.java      # AI camera product scanner
│   ├── OrderPaymentActivity.java          # Checkout & payment
│   ├── UserAddressActivity.java           # Manage delivery address
│   ├── MyordersActivity.java              # View order history
│   ├── CustomerCreateComplaintActivity.java # File a complaint
│   ├── CustomerAddComplaintMessage.java   # Message in complaint thread
│   ├── CustomerReportHistoryActivity.java # View complaint history
│   ├── CustomerReportDetailsActivity.java # View complaint details
│   └── NotificationHistoryActivity.java   # View notifications
│
├── CSRClass/
│   ├── CSRDashboardActivity.java          # CSR home
│   ├── CSRSupportTicketActivity.java      # View support tickets
│   ├── CSRSupportTicketsActivity.java     # List of tickets
│   ├── CSRTicketDetailsActivity.java      # Ticket detail view
│   └── CSRRespondTicketActivity.java      # Respond to tickets
│
├── DeliveryTracking/
│   └── DeliveryTrackingActivity.java      # Track & update delivery status
│
├── Models/        (Data classes: User, Product, Order, OrderItems, Payment, Address, Complaint, DeliveryInfo, Message, Notification, Wishlist)
├── Adapters/      (RecyclerView adapters)
├── DashboardAdapter/ (Admin dashboard menu grid)
├── ProductAdapter/   (Product list adapters)
├── SellerOrdersAdapter/
├── UserAdapter/
└── Utility/
    ├── TfliteEmbeddingHelper.java         # AI embedding extraction
    └── MyFirebaseMessagingService.java    # FCM push notification handler
```

---

## Firebase Database Structure

```
/root
├── users/{userId}           → User (id, fullName, email, password, phone, role, status, joinDate, lastActive, avatarUrl, fcmToken)
├── products/{productId}     → Product (productID, sellerID, productName, productDescription, image, price, quantity, status, approvedBy, embedding[])
├── orders/{orderId}         → Order (orderID, customerID, sellerID, addressID, paymentMethod, deliveryMethod, trackingNo, orderDate, status, totalAmount, orderTimestamp)
├── order_items/{itemId}     → OrderItems (itemID, orderID, productID, price, quantity)
├── payments/{paymentId}     → Payment (paymentID, orderID, method, message, status, paidAt, reference)
├── addresses/{addressId}    → Address (addressID, userID, type, street, city, province, postalCode, country, contactNo, defaultAddress)
├── wishlists/{wishlistId}   → Wishlist (customerID, productID)
├── complaints/{complaintId} → Complaint (complaintID, customerID, orderID, productID, message, status, repliedBy, supportReply, dateCreated, lastUpdated, replyDate)
├── notifications/{userId}/{notifId} → Notification
└── ...
```

---

## Application Flow

### 1. Authentication Flow

```
App Launch
    │
    ▼
┌─────────────┐     ┌──────────────────┐     ┌─────────────────────┐
│ Login Screen │────▶│ Register Screen  │     │ Forgot Password     │
│ (LAUNCHER)   │────▶│ (Pick role:      │     │ (Email reset)       │
│              │     │  Customer/Seller/ │     └─────────────────────┘
│ Email + Pass │     │  CSR)             │
└──────┬───────┘     └──────────────────┘
       │
       │ Firebase Auth → users/{uid}/role
       │
       ├── role = "admin"   ──▶ AdminDashboardActivity
       ├── role = "seller"  ──▶ SellerDashboardActivity
       ├── role = "customer"──▶ CustomerDashboardActivity
       └── role = "csr"     ──▶ CSRDashboardActivity
```

**Registration:** User fills in fullName, email, phone, password, selects a role (Customer/Seller/CSR), accepts Terms & Conditions. Data is saved to Firebase Auth + Realtime Database under `users/{uid}`.

**Login:** Email/password validated via Firebase Auth. Role is fetched from `users/{uid}/role` and used to route to the correct dashboard.

---

### 2. Customer Flow

```
Customer Dashboard
    │
    ├──▶ Browse Products ──▶ View Product Details ──▶ Place Order
    │                              │
    │                              ▼
    │                     Order Payment Screen
    │                     (Select payment method,
    │                      confirm delivery address)
    │                              │
    │                              ▼
    │                     Creates: Order + OrderItem + Payment
    │                              │
    │                              ▼
    ├──▶ My Orders ──▶ Order Details ──▶ Delivery Tracking
    │                                         │
    │                              (Timeline: Processing → Out for Delivery → Delivered)
    │
    ├──▶ AI Product Scanner ──▶ CameraX capture ──▶ TFLite embedding
    │                           ──▶ Cosine similarity match
    │                           ──▶ Opens Browse with matched product name
    │
    ├──▶ Complaints ──▶ Report History ──▶ Report Details
    │       │
    │       ▼
    │   Create Complaint (OrderID + Message)
    │       │
    │       ▼
    │   Add messages in complaint thread
    │
    ├──▶ Notifications
    ├──▶ Settings (Edit Profile, Change Password, Toggle Notifications)
    └──▶ Logout
```

**Key Customer Behaviors:**
- Only **approved** products are shown in Browse
- Product images are stored as Base64 strings in Firebase
- Wishlist/favorites are tracked per customer
- AI Scanner uses MobileNetV2 embeddings + cosine similarity (threshold > 0.4)
- Orders start with status `Pending`, payment is auto-marked `Paid`
- Delivery tracking is read-only for customers (seller/admin updates status)

---

### 3. Seller Flow

```
Seller Dashboard
    │
    ├──▶ Product Management
    │       ├── Add Product (name, description, image, price, quantity)
    │       │       → Saved with status = "Pending" (awaiting admin approval)
    │       │       → AI embedding generated via TFLite
    │       └── Edit / Delete Product
    │
    ├──▶ Order Monitoring
    │       └── Order Details
    │               └── Delivery Tracking
    │                       ├── Start Delivery (select carrier, enter tracking #)
    │                       ├── Update: "Out for Delivery"
    │                       └── Update: "Delivered"
    │
    ├──▶ Profile
    ├──▶ Notifications
    ├──▶ Settings
    └──▶ Logout
```

**Key Seller Behaviors:**
- Products require **admin approval** before appearing in customer browse
- Seller can only see/manage their own products (filtered by `sellerID`)
- Seller updates order delivery status (Processing → Out for Delivery → Delivered)
- Dashboard shows product count, sales count, pending orders, recent activity

---

### 4. Admin Flow

```
Admin Dashboard
    │
    ├──▶ User Management ──▶ Edit User
    │       (View all users, edit roles, status)
    │
    ├──▶ Product Management ──▶ Edit Product
    │       (Approve / Reject pending products)
    │
    ├──▶ Order Monitoring ──▶ Order Details
    │       (View all orders across all sellers)
    │
    ├──▶ Payment Tracking
    │       (Monitor all payments, calculate total revenue)
    │
    ├──▶ View Complaints ──▶ Complaint Conversation
    │       (View complaint threads between customers and support)
    │
    ├──▶ Generate Report ──▶ PDF Export (iTextG)
    │
    ├──▶ Notifications
    ├──▶ Settings
    └──▶ Logout
```

**Key Admin Behaviors:**
- Dashboard shows counters: Total Users, Pending Product Approvals, Total Revenue
- Revenue = sum of `totalAmount` for orders with payment status `Paid`
- Admin approves products → status changes from `Pending` to `Approved`
- Admin can manage all users (edit role, status, profile)
- Admin can generate PDF reports

---

### 5. CSR (Customer Support) Flow

```
CSR Dashboard
    │
    ├──▶ Support Tickets ──▶ Ticket Details ──▶ Respond to Ticket
    │       (View all tickets)
    │
    ├──▶ Resolve Issues (filtered to "Open" tickets)
    │
    ├──▶ Ticket History
    │
    ├──▶ Profile
    ├──▶ Notifications
    ├──▶ Settings
    └──▶ Logout
```

**Key CSR Behaviors:**
- Views and responds to customer support tickets
- Can filter tickets by status (e.g., "Open")
- Updates ticket status and adds support replies

---

### 6. Complaint / Support Flow (End-to-End)

```
Customer creates complaint (OrderID + Message)
    │
    ▼
Complaint saved to Firebase: complaints/{id}
    status = "Pending"
    │
    ▼
CSR / Admin views complaint in list
    │
    ▼
CSR responds to complaint
    │
    ▼
Customer sees reply in Complaint Details / Thread
    │
    ▼
Status updates: Pending → In Progress → Resolved
```

---

### 7. Order & Delivery Flow (End-to-End)

```
Customer browses products → Views product → Selects quantity
    │
    ▼
Order Payment Screen
    ├── Confirms delivery address (from addresses/{userId})
    ├── Selects payment method (Cash, GCash, etc.)
    └── Confirms payment
         │
         ▼
    Creates:
    ├── orders/{orderId}         (status = "Pending")
    ├── order_items/{itemId}     (product details + quantity)
    └── payments/{paymentId}     (status = "Paid")
         │
         ▼
Seller views order in Order Monitoring
    │
    ▼
Seller opens Delivery Tracking
    ├── Selects carrier + enters tracking number
    └── Clicks "Start Delivery"
         │         status = "processing"
         ▼
    Updates status:
    ├── "Out for Delivery"  → timestamp recorded
    └── "Delivered"         → timestamp recorded
         │
         ▼
Customer sees timeline updates in My Orders → Delivery Tracking
```

---

### 8. Push Notification Flow

```
FCM Token generated on login (all roles)
    │
    ▼
Token stored: users/{uid}/fcmToken
    │
    ▼
MyFirebaseMessagingService receives push notifications
    │
    ▼
Notification displayed to user
    │
    ▼
Notification History screen shows all past notifications
    (Unread notifications show red indicator on bell icon)
```

---

## Data Models Summary

| Model        | Key Fields                                                              |
|--------------|-------------------------------------------------------------------------|
| **User**     | id, fullName, email, phone, role, status, joinDate, lastActive, fcmToken |
| **Product**  | productID, sellerID, productName, description, image (Base64), price, quantity, status, embedding[] |
| **Order**    | orderID, customerID, sellerID, addressID, paymentMethod, status, totalAmount, orderTimestamp |
| **OrderItems** | itemID, orderID, productID, price, quantity                           |
| **Payment**  | paymentID, orderID, method, status, paidAt, reference                  |
| **Address**  | addressID, userID, type, street, city, province, postalCode, country, contactNo, defaultAddress |
| **Complaint** | complaintID, customerID, orderID, productID, message, status, repliedBy, supportReply |
| **DeliveryInfo** | deliveryID, orderID, carrier, trackingNo, status, dateTime          |
| **Wishlist** | customerID, productID                                                   |
| **Notification** | userId, title, body, read, timestamp                                |

---

## Key Features

1. **Role-Based Access Control** - 4 roles with separate dashboards and permissions
2. **AI Product Scanner** - CameraX + TFLite MobileNetV2 for visual product search via cosine similarity
3. **Real-Time Order Tracking** - Delivery timeline with status updates (Processing → Out for Delivery → Delivered)
4. **Product Approval Workflow** - Seller submits → Admin approves → Customer sees
5. **Complaint Management** - Customer files complaint → CSR/Admin responds in thread
6. **Push Notifications** - FCM integration for real-time alerts
7. **PDF Report Generation** - Admin can generate reports using iTextG
8. **Wishlist** - Customers can favorite products
9. **Address Management** - Customers can set/manage default delivery address
10. **Notification Badge** - Red indicator when unread notifications exist

---

## Permissions Required

| Permission              | Purpose                        |
|-------------------------|--------------------------------|
| `INTERNET`              | Firebase backend communication |
| `CAMERA`                | AI product scanner             |
| `WRITE_EXTERNAL_STORAGE`| File/image saving              |
| `READ_EXTERNAL_STORAGE` | File/image reading             |
| `POST_NOTIFICATIONS`    | Push notifications (Android 13+) |

---

## How to Run

1. Ensure `google-services.json` is placed in `app/` directory
2. Open project in Android Studio
3. Sync Gradle dependencies
4. Run on emulator or physical device (minSdk 24, targetSdk 35)
5. Register a new account and select a role
6. Admin account must be created manually in Firebase (set role = "admin" in `users` node)
