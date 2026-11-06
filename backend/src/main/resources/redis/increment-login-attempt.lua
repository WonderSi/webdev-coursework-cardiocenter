local key = KEYS[1]
local windowMs = tonumber(ARGV[1])

local attempts = redis.call("INCR", key)

if attempts == 1 then
    redis.call("PEXPIRE", key, windowMs)
end

return { attempts, redis.call("PTTL", key) }