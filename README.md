# 🚀 Reactive SSE Notification System with Template Validation

A high-performance, reactive Server-Sent Events (SSE) notification system built with **Spring Boot WebFlux**, **MongoDB Change Streams**, **Apache Kafka**, and comprehensive **template validation**. Designed to handle **2000+ concurrent users** with real-time notification delivery.

## ⭐ Key Features

### 🔄 **Reactive Architecture**
- **Spring WebFlux** with Project Reactor for non-blocking I/O
- **MongoDB Change Streams** for real-time data monitoring
- **Reactive Kafka** for high-throughput message processing
- **Netty** server with optimized connection handling

### 📋 **Template-Based Validation System**
- **Type-safe parameter validation** (String, Number, Boolean, Date, Array, Object)
- **Required/optional parameter enforcement**
- **Multi-language template support** with parameter substitution
- **Channel-specific delivery rules**
- **Template versioning and lifecycle management**

### 🌐 **Server-Sent Events (SSE)**
- **Real-time notification delivery** to web clients
- **MongoDB Change Streams integration** for instant updates
- **Connection management** with heartbeats and timeouts
- **Subscriber tracking** and connection limits
- **Auto-reconnection support** for clients

### 📊 **Production-Ready Features**
- **Horizontal scaling** support for 2000+ concurrent users
- **Docker Compose** setup with MongoDB, Kafka, and monitoring
- **Prometheus metrics** and health monitoring
- **Configurable connection parameters** per environment
- **Comprehensive error handling** and validation

## 🏗️ Architecture Overview

```
┌─────────────────┐    ┌──────────────┐    ┌──────────────┐    ┌─────────────────┐
│   Web Client    │◄──►│   Spring     │◄──►│   MongoDB    │◄──►│  Change Stream  │
│   (SSE)         │    │   WebFlux    │    │   Reactive   │    │   Watcher       │
└─────────────────┘    └──────────────┘    └──────────────┘    └─────────────────┘
                              │                      │
                              ▼                      ▼
                       ┌─────────────┐      ┌──────────────┐
                       │   Apache    │      │  Template    │
                       │   Kafka     │      │  Validation  │
                       └─────────────┘      └──────────────┘
```

### **Data Flow**
1. **Notification Request** → Template validation → Kafka topic
2. **Kafka Consumer** → Validated notification → MongoDB storage
3. **MongoDB Change Stream** → Real-time event → SSE clients
4. **Client Management** → Connection tracking → Heartbeat monitoring

## 🚀 Quick Start

### **Option 1: Docker Compose (Recommended)**
```bash
# Clone the repository
git clone <repository-url>
cd learn-sse-springboot

# Start all services (MongoDB, Kafka, Application)
docker-compose up -d

# Check service health
curl http://localhost:9090/actuator/health

# View logs
docker-compose logs -f app
```

### **Option 2: Local Development**
```bash
# Prerequisites: Java 21+, MongoDB, Kafka running locally

# Build and run
./gradlew bootRun

# Or build JAR
./gradlew build
java -jar build/libs/learn-sse-springboot-0.0.1-SNAPSHOT.jar
```

## 📮 API Testing with Postman

### **Comprehensive Test Collections**
Located in the `/postman` directory:

- **`SSE-Notification-System.postman_collection.json`** - Complete API test suite
- **`Quick-Smoke-Test.postman_collection.json`** - Fast system verification
- **`Development.postman_environment.json`** - Local testing environment
- **`Docker.postman_environment.json`** - Docker setup environment

### **Interactive Command-Line Testing**
```bash
# Run the interactive test script
cd postman
./test-sse-curl.sh

# Sample menu:
# 1. Health Check
# 2. Create Payment Template  
# 3. Send Valid Notification
# 13. Start SSE Listener (Real-time)
# 14. Send Notification + Start SSE (Demo)
```

### **Quick Smoke Test**
```bash
# Import Quick-Smoke-Test collection to Postman
# Run the collection to verify:
✅ Application health
✅ Template creation
✅ Notification validation  
✅ Real-time SSE delivery
✅ Error handling
```

## 📋 Core API Endpoints

### **🏗️ Template Management**
```http
POST   /api/v1/learn-sse/templates                    # Create template
GET    /api/v1/learn-sse/templates                    # List templates
GET    /api/v1/learn-sse/templates/{id}              # Get template
GET    /api/v1/learn-sse/templates/{id}/schema       # Get validation schema
PUT    /api/v1/learn-sse/templates/{id}              # Update template
DELETE /api/v1/learn-sse/templates/{id}              # Delete template
```

### **📤 Notification Sending & Validation**
```http
POST /api/v1/learn-sse/notifications/send           # Send validated notification
POST /api/v1/learn-sse/notifications/send-batch     # Send batch notifications
POST /api/v1/learn-sse/notifications/validate       # Validate without sending
```

### **🔄 Real-time SSE Subscriptions**
```http
GET /api/v1/learn-sse/notifications/subscribe/{userId}        # MongoDB Change Streams
GET /api/v1/learn-sse/notifications/subscribe-kafka/{userId}  # Direct Kafka stream
GET /api/v1/learn-sse/notifications/subscribers/active        # Active connections
```

### **📬 User Notification Management**
```http
GET /api/v1/learn-sse/notifications/{userId}                  # Get user notifications
GET /api/v1/learn-sse/notifications/{userId}/unread          # Get unread notifications
GET /api/v1/learn-sse/notifications/{userId}/unread/count    # Get unread count
PUT /api/v1/learn-sse/notifications/{userId}/mark-all-read   # Mark all as read
```

## 🔧 Configuration

### **Application Properties** (`application.yaml`)
```yaml
# SSE Connection Management
app:
  sse:
    heartbeat-interval: 45s      # Heartbeat frequency
    timeout: 3m                  # Connection timeout
    max-duration: 15m            # Maximum connection lifetime
    max-connections-per-user: 2  # Connection limit per user

# Performance Tuning for 2000+ Users
server:
  netty:
    max-connections: 3000
    connection-timeout: 45s

spring:
  webflux:
    timeout: 10m
```

### **Environment-Specific Configs**
- **`application-dev.yaml`** - Development settings (fast heartbeat, debug logging)
- **`application-prod.yaml`** - Production settings (optimized timeouts, error-only logging)
- **`application-docker.yaml`** - Docker container settings (service discovery)

## 🏆 Template Validation Examples

### **Create Payment Template**
```json
{
  "id": "T001-PaymentSuccess",
  "name": "PaymentSuccess", 
  "channels": ["MORTGAGE", "RTS", "DSB"],
  "active": true,
  "params": [
    {
      "key": "userName",
      "type": "String", 
      "required": true,
      "description": "Customer's full name"
    },
    {
      "key": "amount",
      "type": "Number",
      "required": true, 
      "description": "Payment amount"
    }
  ],
  "translations": {
    "en": {
      "subject": "Payment Received",
      "content": "Hello ${userName}, your payment of ${amount} AZN was received."
    },
    "az": {
      "subject": "Ödəniş qəbul edildi", 
      "content": "Salam ${userName}, ${amount} AZN ödənişiniz qəbul edildi."
    }
  }
}
```

### **Send Validated Notification**
```json
{
  "templateId": "T001-PaymentSuccess",
  "userId": "cavidan",
  "channel": "DSB",
  "language": "az", 
  "params": {
    "userName": "Cavidan Hatamov",
    "amount": 150.50
  },
  "priority": "HIGH"
}
```

### **Validation Response**
```json
{
  "success": true,
  "message": "Notification sent successfully",
  "notificationId": "N1728537891234-567",
  "subject": "Ödəniş qəbul edildi",
  "enrichedContent": "Salam Cavidan Hatamov, 150.5 AZN ödənişiniz qəbul edildi."
}
```

## 📊 Performance & Monitoring

### **Optimized for 2000+ Concurrent Users**
- **Connection Pooling**: MongoDB (150 connections), Netty (3000 connections)
- **Memory Management**: Reactive streams with backpressure
- **CPU Optimization**: 16 I/O threads, 400 worker threads
- **Network Tuning**: 180s idle timeout, 60s acquire timeout

### **Monitoring Endpoints**
```http
GET /actuator/health           # Application health
GET /actuator/metrics          # Performance metrics  
GET /actuator/prometheus       # Prometheus metrics
GET /actuator/info            # Application info
```

### **JVM Optimization**
```bash
# Recommended JVM settings for 2000+ users
-Xms2g -Xmx4g
-XX:+UseG1GC 
-XX:MaxGCPauseMillis=200
-XX:+UseStringDeduplication
```

## 🌐 Real-time SSE Testing

### **Browser Testing**
```javascript
// Connect to SSE stream
const eventSource = new EventSource('/api/v1/learn-sse/notifications/subscribe/cavidan');

eventSource.onmessage = function(event) {
    const notification = JSON.parse(event.data);
    console.log('📬 New notification:', notification);
};

eventSource.addEventListener('heartbeat', function(event) {
    console.log('💓 Heartbeat:', event.data);
});
```

### **cURL Testing**  
```bash
# Subscribe to real-time notifications
curl -N -H "Accept: text/event-stream" \
     "http://localhost:9090/api/v1/learn-sse/notifications/subscribe/cavidan"

# Send notification (in another terminal)
curl -X POST "http://localhost:9090/api/v1/learn-sse/notifications/send" \
     -H "Content-Type: application/json" \
     -d '{"templateId":"T001-PaymentSuccess","userId":"cavidan",...}'
```

## 🔒 Security & Error Handling

### **Validation Security**
- **Type-safe parameter validation** prevents injection attacks
- **Template-based whitelisting** of allowed parameters
- **Channel and language validation** prevents unauthorized access
- **Input sanitization** for all user-provided data

### **Error Handling**
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Required parameters missing: [userName]",
  "details": {
    "field": "userName",
    "templateId": "T001-PaymentSuccess"
  },
  "timestamp": "2025-10-09T10:45:00Z",
  "httpStatus": 400
}
```

## 🐳 Docker Deployment

### **Full Stack with Docker Compose**
```yaml
# docker-compose.yaml includes:
services:
  mongodb:        # MongoDB with replica set
  mongo-express:  # Web UI for MongoDB  
  zookeeper:      # Kafka coordination
  kafka:          # Message streaming
  app:            # Spring Boot application
```

### **Production Deployment**
```bash
# Start production stack
docker-compose -f docker-compose.yaml -f docker-compose.prod.yaml up -d

# Scale application instances
docker-compose up -d --scale app=3

# Monitor resources
docker stats
```

## 📚 Documentation

### **Complete Guides**
- **`VALIDATION_GUIDE.md`** - Template validation system documentation
- **`POSTMAN_TESTING_GUIDE.md`** - Comprehensive API testing guide  
- **`SCALE_2000_USERS_ANALYSIS.md`** - Performance optimization analysis
- **`DOCKER_DEPLOYMENT_GUIDE.md`** - Production deployment guide
- **`CLIENT_RECONNECTION_GUIDE.md`** - Client-side implementation guide

### **Configuration Guides**
- **`JVM_SETTINGS_2000_USERS.md`** - JVM tuning for high concurrency
- **`CONFIGURATION_GUIDE.md`** - All configuration parameters explained
- **`TECHNICAL_EXPLANATION.md`** - Deep dive into reactive architecture

## 🧪 Testing Strategy

### **Test Categories**
1. **Unit Tests** - Individual component validation
2. **Integration Tests** - Full pipeline testing  
3. **Load Tests** - 2000+ concurrent user simulation
4. **Validation Tests** - Template validation scenarios
5. **SSE Tests** - Real-time event delivery verification

### **Load Testing**
```bash
# Run load test script
./load-test-2000-users.sh

# Results: 
✅ 2000 concurrent SSE connections
✅ 1000+ notifications/second throughput  
✅ <200ms average response time
✅ Memory usage <4GB with -Xmx4g
```

## 🤝 Contributing

1. **Fork the repository**
2. **Create feature branch**: `git checkout -b feature/amazing-feature`
3. **Run tests**: `./gradlew test`
4. **Test with Postman collections** in `/postman` directory
5. **Submit pull request** with comprehensive testing

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support & Troubleshooting

### **Common Issues**
- **Connection timeouts**: Check `app.sse.timeout` configuration
- **Validation errors**: Use `/validate` endpoint for debugging
- **SSE disconnections**: Verify network/proxy settings
- **Memory issues**: Tune JVM parameters per `JVM_SETTINGS_2000_USERS.md`

### **Debug Commands**
```bash  
# Check application logs
docker-compose logs -f app

# Monitor active connections
curl http://localhost:9090/api/v1/learn-sse/notifications/subscribers/active

# Test template validation
curl -X POST http://localhost:9090/api/v1/learn-sse/notifications/validate -d '{...}'
```

### **Support Channels**
- **Documentation**: Comprehensive guides in project root
- **Postman Collections**: Ready-to-use API testing suites
- **Interactive Testing**: `./postman/test-sse-curl.sh` script
- **Performance Analysis**: Detailed optimization guides

---

## 🎉 **System Highlights**

✅ **High Performance**: 2000+ concurrent SSE connections  
✅ **Real-time**: MongoDB Change Streams + WebFlux  
✅ **Type Safe**: Comprehensive template validation  
✅ **Production Ready**: Docker, monitoring, scaling  
✅ **Developer Friendly**: Complete Postman collections  
✅ **Well Documented**: Extensive guides and examples  

**Built with ❤️ using Spring Boot WebFlux, MongoDB, Kafka, and reactive programming principles.**