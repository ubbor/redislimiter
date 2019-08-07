--redis limiter
--ericprotectearth@gmail.com
--2019/7/25

-- key前缀
local key_prefix = KEYS[1]
-- 资源标识
local target = KEYS[2]
-- 时间窗口内最大并发数
local max_concurrent = tonumber(ARGV[1] or 0)
-- 并发时间窗口长度(秒)
local concurrent_window = tonumber(ARGV[2] or 0)


local breaker_key = string.format("%s:%s:breaker", key_prefix, target)
local current_permits_key = string.format("%s:%s:currentpermits", key_prefix, target)
local test_permit_key = string.format("%s:%s:testpermit", key_prefix, target)

local breaker = redis.call("get", breaker_key)
if breaker == "OPEN" then
    local test_exists = tonumber(redis.call("EXISTS", test_permit_key) or 0)
    if test_exists == 0 then
        redis.call("set", test_permit_key, 1)
        redis.call("EXPIRE", test_permit_key, 60)
        return cjson.encode({ status = "TEST" })
    end
    return cjson.encode({ status = "BREAKER_OPEN" })
end

local current_permits = tonumber(redis.call("incr", current_permits_key))
if current_permits == 1 then
    redis.call("EXPIRE", current_permits_key, concurrent_window)
end
if current_permits > max_concurrent then
    return cjson.encode({ status = "LIMITER_REFUSED", current_permits = current_permits, max_concurrent = max_concurrent, concurrent_window = concurrent_window })
end
--把更多的信息返回回去,方便调试
return cjson.encode({ status = "OK", current_permits = current_permits })
