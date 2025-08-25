# Google Maps Weather API 模块

本模块实现了对 Google Maps Weather API 的完整封装，包括当前天气状况和每小时天气预报功能，并集成了基于地理位置的智能缓存系统。

## 特性

- ✅ 完整的 Google Maps Weather API 封装
- ✅ 当前天气状况查询
- ✅ 每小时天气预报查询
- ✅ **基于地理位置的智能缓存系统**
- ✅ 支持多种单位系统（公制、英制等）
- ✅ 完整的错误处理和日志记录
- ✅ 自动过期缓存清理

## 缓存系统

### 设计特点
- 使用 Redis 的 Geo 数据类型存储地理位置信息
- 10km 范围内的查询会命中相同的缓存
- 缓存有效期：30分钟
- 自动清理过期缓存：每15分钟执行一次

### 缓存策略
1. 新的天气查询请求到来时，首先在10km范围内搜索是否有缓存
2. 如果找到有效缓存，直接返回缓存数据
3. 如果没有找到缓存或缓存已过期，调用API获取新数据并缓存
4. 定时任务自动清理过期的缓存条目

## API 端点

### 当前天气状况

#### 基础查询
```
GET /api/weather/current?latitude={lat}&longitude={lng}
```

#### 指定单位系统
```
GET /api/weather/current?latitude={lat}&longitude={lng}&units={METRIC|IMPERIAL}
```

### 每小时天气预报

#### 基础查询
```
GET /api/weather/hourly?latitude={lat}&longitude={lng}
```

#### 高级查询
```
GET /api/weather/hourly?latitude={lat}&longitude={lng}&units={METRIC|IMPERIAL}&hours={24}&pageSize={10}&pageToken={token}
```

### 缓存管理

#### 手动清理过期缓存
```
POST /api/weather/cache/cleanup
```

## 配置

### application.properties 配置项

```properties
# Google Maps Weather API 配置
google.maps.weather.api-key=YOUR_API_KEY
google.maps.weather.base-url=https://weather.googleapis.com/v1
google.maps.weather.connect-timeout=5000
google.maps.weather.read-timeout=10000
google.maps.weather.query-rate-limit=50
google.maps.weather.default-units-system=METRIC
google.maps.weather.enable-request-logging=false

# 天气缓存配置
weather.cache.radius-km=10
weather.cache.duration-minutes=30
weather.cache.cleanup-interval-minutes=15
```

## 架构设计

### 模块结构
```
com.rwm.weather/
├── cache/                  # 缓存相关类
│   ├── WeatherCacheEntry.java
│   ├── WeatherGeoCacheService.java
│   └── WeatherCacheCleanupTask.java
├── client/                 # HTTP 客户端
│   ├── WeatherApiClient.java
│   └── WeatherApiException.java
├── config/                 # 配置类
│   ├── WeatherConfig.java
│   ├── WeatherHttpClientConfig.java
│   └── WeatherCacheConfig.java
├── controller/             # 控制器
│   ├── WeatherController.java
│   └── WeatherCacheController.java
├── dto/                    # 数据传输对象
│   ├── CurrentConditionsResponse.java
│   ├── HourlyForecastResponse.java
│   ├── Location.java
│   ├── UnitsSystem.java
│   ├── WeatherConditionType.java
│   ├── PrecipitationType.java
│   └── ...
└── service/               # 服务层
    ├── WeatherService.java
    └── impl/
        └── WeatherServiceImpl.java
```

### 缓存架构

1. **Redis Geo存储**：使用Redis的GEOADD命令存储天气查询的地理位置
2. **数据分离**：地理位置信息和天气数据分别存储，避免数据冗余
3. **智能查询**：通过GEORADIUS命令在指定半径内查找相近的缓存条目
4. **自动过期**：使用Redis的TTL机制和定时任务双重保证缓存过期清理

## 工程实践优势

### 1. 性能优化
- **减少API调用**：10km范围内的重复查询直接使用缓存，显著减少对Google API的调用
- **快速响应**：缓存命中时响应时间从几百毫秒降至几十毫秒
- **带宽节省**：减少不必要的网络请求

### 2. 成本控制
- **API配额管理**：通过缓存有效控制API调用次数，降低费用
- **智能去重**：相近位置的查询自动合并，避免重复收费

### 3. 可靠性
- **降级策略**：缓存失败时仍能正常调用API
- **自动清理**：定时任务确保缓存不会无限增长
- **异常隔离**：缓存异常不影响主要业务流程

### 4. 可扩展性
- **配置化**：缓存半径、过期时间等参数可配置
- **模块化**：缓存系统独立，易于扩展和维护

## 错误处理

- 网络超时：配置连接和读取超时时间
- API限制：内置速率限制保护
- 数据解析：完整的JSON反序列化错误处理
- 缓存异常：缓存失败时仍能正常返回API数据

## 日志记录

- 详细的请求/响应日志
- 缓存命中/未命中日志
- 错误日志和异常堆栈
- 性能监控日志

## 使用建议

1. **API Key 管理**：确保API Key有足够的配额和正确的权限
2. **缓存优化**：根据业务需求调整缓存半径和过期时间
3. **监控告警**：监控API调用频率和缓存命中率
4. **错误处理**：实现适当的降级策略和用户友好的错误提示
