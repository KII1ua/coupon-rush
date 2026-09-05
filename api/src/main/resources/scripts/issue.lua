local limit = redis.call('GET', KEYS[2])
if not limit then
    return -2
end
if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then
    return -1
end
if redis.call('SCARD', KEYS[1]) >= tonumber(limit) then
    return 0
end
redis.call('SADD', KEYS[1], ARGV[1])
return 1