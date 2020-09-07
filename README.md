
# 单元测试
## 为单元测试启动一个本地Redis
```
sudo docker run -d --name dual-cache-redis \
-p 6379:6379 redis:4.0.14-alpine \
redis-server --appendonly yes
```
