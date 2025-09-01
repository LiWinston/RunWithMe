# Google Maps Weather API 集成

本模块实现了对Google Maps Platform Weather API的完整封装，提供当前天气状况、每小时预报、每日预报等功能。

## 目录结构

```
weather/
├── client/           # HTTP客户端
├── config/          # 配置类
├── controller/      # REST控制器
├── dto/            # 数据传输对象
└── service/        # 服务层
```

## 配置说明

### 1. 获取Google Maps API密钥

1. 访问 [Google Cloud Console](https://console.cloud.google.com/)
2. 创建或选择一个项目
3. 启用 Weather API
4. 创建API密钥并设置适当的限制

### 2. 配置文件设置

在 `application.properties` 中配置：

```properties
# Google Maps Weather API配置
google.maps.weather.api-key=YOUR_ACTUAL_API_KEY
google.maps.weather.base-url=https://weather.googleapis.com/v1
google.maps.weather.connect-timeout=5000
google.maps.weather.read-timeout=10000
google.maps.weather.query-rate-limit=50
google.maps.weather.default-units-system=METRIC
google.maps.weather.enable-request-logging=false
```

## API接口说明

### 1. 获取当前天气状况

#### GET /api/weather/current

**请求参数：**
- `latitude` (必需): 纬度，范围 -90 到 90
- `longitude` (必需): 经度，范围 -180 到 180  
- `unitsSystem` (可选): 单位制，METRIC（公制）或 IMPERIAL（英制），默认 METRIC

**示例请求：**
```
GET /api/weather/current?latitude=39.9042&longitude=116.4074&unitsSystem=METRIC
```

#### POST /api/weather/current

**请求体：**
```json
{
  "latitude": 39.9042,
  "longitude": 116.4074
}
```

**请求参数：**
- `unitsSystem` (可选): 单位制，默认 METRIC

### 2. 健康检查

#### GET /api/weather/health

检查Weather API服务状态。

## 响应格式

所有API响应都使用统一的 `Result<T>` 格式：

```json
{
  "code": 0,
  "message": "获取当前天气状况成功",
  "data": {
    "currentTime": "2025-01-28T22:04:12.025273178Z",
    "timeZone": {
      "id": "Asia/Shanghai"
    },
    "isDaytime": true,
    "weatherCondition": {
      "iconBaseUri": "https://maps.gstatic.com/weather/v1/sunny",
      "description": {
        "text": "Sunny",
        "languageCode": "en"
      },
      "type": "CLEAR"
    },
    "temperature": {
      "degrees": 13.7,
      "unit": "CELSIUS"
    },
    "feelsLikeTemperature": {
      "degrees": 13.1,
      "unit": "CELSIUS"
    },
    "relativeHumidity": 42,
    "uvIndex": 1,
    "precipitation": {
      "probability": {
        "percent": 0,
        "type": "RAIN"
      },
      "qpf": {
        "quantity": 0,
        "unit": "MILLIMETERS"
      }
    },
    "wind": {
      "direction": {
        "degrees": 335,
        "cardinal": "NORTH_NORTHWEST"
      },
      "speed": {
        "value": 8,
        "unit": "KILOMETERS_PER_HOUR"
      },
      "gust": {
        "value": 18,
        "unit": "KILOMETERS_PER_HOUR"
      }
    },
    "visibility": {
      "distance": 16,
      "unit": "KILOMETERS"
    },
    "cloudCover": 0
  }
}
```

## 主要数据结构

### WeatherCondition (天气状况)
- `iconBaseUri`: 天气图标URL
- `description`: 天气描述（文本+语言）
- `type`: 天气类型枚举

### Temperature (温度)
- `degrees`: 温度数值
- `unit`: 温度单位（CELSIUS/FAHRENHEIT）

### Wind (风信息)
- `direction`: 风向（角度+基本方向）
- `speed`: 风速
- `gust`: 阵风

### Precipitation (降水)
- `probability`: 降水概率和类型
- `qpf`: 降水量

## 错误处理

- **参数验证错误**: code=1, 返回具体的参数错误信息
- **API调用失败**: code=1, 返回"获取天气信息失败"及具体错误信息
- **网络错误**: 自动重试机制（根据配置）

## 使用示例

### Java代码调用

```java
@Autowired
private WeatherService weatherService;

// 获取北京的当前天气（公制单位）
CurrentConditionsResponse weather = weatherService.getCurrentConditions(39.9042, 116.4074);

// 获取纽约的当前天气（英制单位）
Location location = new Location(40.7128, -74.0060);
CurrentConditionsResponse weather = weatherService.getCurrentConditions(location, UnitsSystem.IMPERIAL);
```

### HTTP请求示例

```bash
# 获取北京当前天气
curl "http://localhost:8080/api/weather/current?latitude=39.9042&longitude=116.4074"

# 获取纽约当前天气（英制单位）
curl "http://localhost:8080/api/weather/current?latitude=40.7128&longitude=-74.0060&unitsSystem=IMPERIAL"
```

## 注意事项

1. **API配额**: Google Maps Weather API有使用配额限制，请合理使用
2. **缓存**: 建议在生产环境中实现适当的缓存机制
3. **错误重试**: 已内置基本的错误处理，可根据需要扩展重试逻辑
4. **安全性**: 确保API密钥的安全，不要提交到版本控制系统

## 扩展功能

当前实现了当前天气状况功能，可以进一步扩展：

- 每小时天气预报 (forecast/hours)
- 每日天气预报 (forecast/days) 
- 历史天气数据 (history/hours)
- 缓存机制
- 批量查询
- 速率限制
- 监控和指标收集

## 相关文档

- [Google Maps Weather API官方文档](https://developers.google.com/maps/documentation/weather)
- [API参考文档](https://developers.google.com/maps/documentation/weather/reference/rest)
