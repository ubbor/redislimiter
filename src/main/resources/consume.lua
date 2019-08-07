--redis limiter
--ericprotectearth@gmail.com
--2019/7/25

-- 状态:  String Ok/熔断/限流/测试
-- 时间窗口内请求数量 String
-- 时间窗口内错误数量 String
-- 如果已经熔断，并且不存在测试窗口，则颁发一个测试信号出去。
-- 测试窗口内的测试结果，如果测试窗口存在，就不再颁发测试测试信号出去。
-- 如果测试状态更新回来-请求成功，重置 时间窗口内请求数量 和 时间窗口内错误数量 ,测试窗口key
-- 如果测试状态更新回来-请求失败，保持时间窗口。

-- key前缀
local key_prefix = KEYS[1]
-- 资源标识
local target = KEYS[2]
-- 请求时拿到的状态
local require_status = ARGV[1]
-- 执行状态 SUCCESS/EXCEPTION
local execute_result = ARGV[2]
-- 时间窗口内最大并发数
local max_concurrent = tonumber(ARGV[3] or 0)
-- 触发熔断的错误率
local breaker_open_rate = tonumber(ARGV[4] or 100)
-- 触发熔断时的最小错误数
local breaker_open_count = tonumber(ARGV[5] or 0)
-- 时间窗口大小 单位秒
local window_second = tonumber(ARGV[6] or 0)
--当前并发数量
local current_permits_key = string.format("%s:%s:currentpermits", key_prefix, target)
--时间窗口成功数
local cuccess_in_window_key = string.format("%s:%s:cuccessinwindow", key_prefix, target)
--时间窗口失败数
local exception_in_window_key = string.format("%s:%s:exceptioninwindow", key_prefix, target)
local test_permit_key = string.format("%s:%s:testpermit", key_prefix, target)
local breaker_key = string.format("%s:%s:breaker", key_prefix, target)

--如果是测试信号,测试成功,重置数据.测试失败,继续熔断.
if require_status == "\"TEST\"" and execute_result == "\"SUCCESS\"" then
    redis.call("DEL", cuccess_in_window_key)
    redis.call("DEL", exception_in_window_key)
    redis.call("set", breaker_key, "CLOSE")
    --    return cjson.encode({
    --        breaker = "CLOSE"
    --    })
end
-- 记录成功/失败数量
-- 初始化
local exists = tonumber(redis.call("EXISTS", cuccess_in_window_key) or 0)
if exists == 0 then
    redis.call("mset", cuccess_in_window_key, 0, exception_in_window_key, 0)
    redis.call("EXPIRE", cuccess_in_window_key, window_second)
    redis.call("EXPIRE", exception_in_window_key, window_second)
end
if execute_result == "SUCCESS" then
    redis.call("incr", cuccess_in_window_key)
else
    redis.call("incr", exception_in_window_key)
end

-- 失败的时候计算一下,是否满足熔断条件
if execute_result ~= "\"SUCCESS\"" and require_status == "\"OK\"" then
    --    return cjson.encode({ execute_result = execute_result,require_status=require_status})
    local cuccess_in_window = tonumber(redis.call("get", cuccess_in_window_key) or 0)
    local exception_in_window = tonumber(redis.call("get", exception_in_window_key) or 0)
    --触发熔断,并记录测试信号的颁发间隔(1分钟)
    if exception_in_window >= breaker_open_count and exception_in_window / (cuccess_in_window + exception_in_window) > (breaker_open_rate / 100) then
        redis.call("set", test_permit_key, 1)
        redis.call("EXPIRE", test_permit_key, 60)
        redis.call("set", breaker_key, "OPEN")
        return cjson.encode({
            breaker = "OPEN",
            exception_in_window = exception_in_window,
            cuccess_in_window = cuccess_in_window
        })
    end
end
return cjson.encode({
    status = "succcess",
    exists = exists,
    execute_result = execute_result,
    window_second = window_second,
    max_concurrent = max_concurrent,
    breaker_open_rate = breaker_open_rate,
    breaker_open_count = breaker_open_count
})
