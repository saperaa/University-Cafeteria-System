# 🍽️ University Cafeteria Order & Loyalty System

A comprehensive Java-based cafeteria management system featuring modern GUI interface, loyalty points system, and advanced OOP design principles.

## 👥 Team Members

- **Mohamed Alaa** - Project Lead & Developer
- **Arsany Refaat** - Developer & System Architecture
- **Kenzy Ahmed** - Developer & UI/UX Design

## 🚀 Features

### 🎯 Core Functionality
- **Dual Interface**: Modern JavaFX GUI and Console interface
- **User Management**: Student and Staff authentication with role-based access
- **Menu Management**: Comprehensive menu system with categories (Main Course, Snacks, Drinks, Desserts, Breakfast)
- **Order Processing**: Complete order lifecycle from placement to completion
- **Loyalty System**: Points earning and redemption with transaction history

### 💡 Advanced Features
- **Real-time Order Tracking**: Order status updates (Pending → Confirmed → Preparing → Ready → Completed)
- **Loyalty Points**: Earn 1 point per 10 EGP spent, redeem for discounts
- **Inventory Management**: Track item availability and stock
- **Reporting System**: Daily, weekly, and monthly sales reports
- **Notification System**: Order updates and promotional notifications
- **Database Integration**: H2 embedded database with MySQL connector support

### 🏗️ Technical Highlights
- **Modern Java**: Java 23 with latest features
- **JavaFX**: Rich desktop GUI application
- **SOLID Principles**: Clean architecture and design patterns
- **Maven Build**: Professional project structure with dependency management
- **Unit Testing**: Comprehensive test coverage with JUnit 5 and Mockito
- **Stream API**: Modern Java 8+ features for data processing

## 🛠️ Technology Stack

- **Language**: Java 23
- **GUI Framework**: JavaFX 23.0.1
- **Build Tool**: Maven 3.x
- **Database**: H2 Database (embedded) + MySQL Connector
- **Testing**: JUnit 5, Mockito
- **IDE**: Compatible with IntelliJ IDEA, Eclipse, VS Code

## 📋 Prerequisites

- Java 23 or higher
- Maven 3.6+
- Git (for cloning)

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/university-cafeteria-system.git
cd university-cafeteria-system
```

### 2. Build the Project
```bash
mvn clean compile
```

### 3. Run the Application

#### GUI Mode (Recommended)
```bash
mvn javafx:run
```

#### Console Mode
```bash
mvn exec:java -Dexec.mainClass="com.university.cafeteria.Main" -Dexec.args="console"
```

### 4. Run Tests
```bash
mvn test
```

## 🎮 Usage

### GUI Interface
1. Launch the application using `mvn javafx:run`
2. Login with student or staff credentials
3. Browse menu items and add to cart
4. Place orders and track status
5. Manage loyalty points and view transaction history

### Console Interface
1. Run with console argument: `mvn exec:java -Dexec.mainClass="com.university.cafeteria.Main" -Dexec.args="console"`
2. Follow the interactive menu prompts
3. Navigate through different system functions

## 📁 Project Structure

```
src/
├── main/java/com/university/cafeteria/
│   ├── Main.java                    # Application entry point
│   ├── gui/                         # JavaFX GUI components
│   │   ├── CafeteriaGUI.java        # Main GUI application
│   │   ├── controllers/             # GUI controllers
│   │   └── styles/                  # UI styling
│   ├── model/                       # Domain models
│   │   ├── MenuItem.java            # Menu item entity
│   │   ├── Order.java               # Order management
│   │   ├── LoyaltyAccount.java      # Loyalty points system
│   │   ├── Student.java             # Student user model
│   │   └── Staff.java               # Staff user model
│   ├── service/                     # Business logic services
│   │   ├── CafeteriaSystem.java     # Main system orchestrator
│   │   ├── AuthenticationService.java
│   │   ├── OrderService.java
│   │   ├── LoyaltyService.java
│   │   └── ReportingService.java
│   ├── repository/                  # Data access layer
│   │   ├── UserRepository.java
│   │   ├── MenuRepository.java
│   │   └── OrderRepository.java
│   └── ui/                          # Console interface
└── test/java/                       # Unit tests
```

## 🎨 Screenshots

The application features a modern, intuitive interface with:
- Clean login screen
- Interactive menu browsing
- Real-time order tracking
- Loyalty points management
- Comprehensive reporting dashboard

## 🔧 Configuration

### Database Configuration
The system uses H2 embedded database by default. To use MySQL:

1. Update `pom.xml` dependencies
2. Configure database connection in service classes
3. Run database migration scripts

### Customization
- Modify `StyleConstants.java` for UI theming
- Update menu items in `MenuService`
- Configure loyalty points rules in `LoyaltyService`

## 🧪 Testing

Run the complete test suite:
```bash
mvn test
```

Run specific test categories:
```bash
mvn test -Dtest=OrderTest
mvn test -Dtest=*ServiceTest
```

## 📊 Key Metrics

- **Lines of Code**: 2000+ lines
- **Test Coverage**: 85%+
- **Classes**: 25+ classes
- **Design Patterns**: Repository, Service Layer, MVC
- **Java Features**: Streams, Lambda expressions, Optional, Records

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- University faculty for project guidance
- JavaFX community for excellent documentation
- Maven ecosystem for robust build tools
- Open source contributors for inspiration

## 📞 Contact

- **Mohamed Alaa**: [GitHub Profile](https://github.com/mohamedalaa)
- **Arsany Refaat**: [GitHub Profile](https://github.com/arsanyrefaat)
- **Kenzy Ahmed**: [GitHub Profile](https://github.com/kenzyahmed)

---

⭐ **Star this repository if you found it helpful!**

🍽️ **Happy coding and enjoy your cafeteria experience!**
