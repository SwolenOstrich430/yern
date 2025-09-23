# Overview 
This is the repo for a knitting and crochet tool -- the name is in progress. 

## Local Setup 

### Install Java
brew install java 

### Install Postgres 
brew install postgres 

### Authenticate to Cloud 
#### GCP 
Follow the insturctions in *./cloud/gcp/auth/README.md*.


## Definitions 

* Panel: a section of a design. See also: piece(s), section, squares (granny square blankets).
* Piece(s): either a garment of clothing in its entirety or a section of a design. 
* Project: one or more pieces that a user is working on. See also: set. 
* Section: synonmy for panel.
* Set: A project with mulitple pieces.
* Working in "The Round": creating a continuous piece that isn't sewn together from separate panels. 

## Features 

### User Accounts 

#### Registration 

#### Login

### Stitch Counter 

#### Database 

#### Cache
##### Redis 
<b>Overview</b>

<b>Methods and References</b>

<b>Spring Classes</b>
  * RedisTemplate: central class for Redis module. The template takes care of serialization and connection management, freeing the user from dealing with such details. Implements the RedisOperations interface. Lower-level access to Redis' actual, underlying methods. 
  * ReactiveRedisTemplate: mirror of RedisTemplate and implements the ReactiveRedisOperations interface. 
  * RedisConnection: "provides the core building block for Redis communication, as it handles the communication with the Redis backend. It also automatically translates underlying connecting library exceptions to Spring’s consistent DAO exception hierarchy so that you can switch connectors without any code changes, as the operation semantics remain the same." Spring's implementation of RedisConnection/LettuceConnection aren't thread-safe. Lettuce's StatefulRedisConnection is though. 
  * RedisStandaloneConfiguration: one server to handle reads and writes. 
  * RedisSentinelConfiguration: master for writes and sentinel for reads 
  * RedisClusterConnection: See https://docs.spring.io/spring-data/redis/reference/redis/connection-modes.html. 
  * RedisConnectionFactory: creates RedisConnections (connection pooling?) and provides global exception translating.  
  * RedisCacheManager: main provider for redis established as a bean in a config package. Makes use of a build method to provide settings (RedisCacheManagerBuilder).
  * RedisCacheConfiguration: "lets you set key expiration times, prefixes, and RedisSerializer implementations for converting to and from the binary storage format" (<link href="https://docs.spring.io/spring-data/redis/reference/redis/redis-cache.html">Spring Redis Docs</link>). Can configure a global TTL via its enableTtl(Duration duration) function.  A custom implementation of Duration getTimeToLive can also be added at a per-cache-entry basis by supplying a custom TTLFunction.INSTANCE. A combination of global and per-cache configuration can also be provided. 
  * RedisCacheWriter: required by RedisCacheManagerBuilder, provides create, update, delete operations and can manage TTL expiration dynamically on a per-cache basis. Allows the latter with RedisCacheWriter.TtlFunction (after 3.2.0).

<b>Lettuce</b>
* package: org.springframework.data.redis.connection.lettuce
* Can be configured with a LettuceConnectionFactory Bean (redisConnectionFactory) in a configuration class 

<b>FYIs</b>
* cache key format = "<cache_name_prefix><cache_name>::<the_key>"
* Time-to-Live (TTL): set and reset by create or update operation. NOT RESET ON GET OPERATIONS.
* Time-to-Idle (TTI) expiration: reset by read and update operatinos. 

<b>Questions</b>
* Lock-free vs. locking RedisCacheWriter (defaults to lock-free -- <link href="https://docs.spring.io/spring-data/redis/reference/redis/redis-cache.html">The lack of entry locking can lead to overlapping, non-atomic commands for the Cache putIfAbsent and clean operations, as those require multiple commands to be sent to Redis. The locking counterpart prevents command overlap by setting an explicit lock key and checking against presence of this key, which leads to additional requests and potential command wait times"</link>)
* What should the common format of keys be? E.g., "<resource>_<method>"?
* What should the cache interface look like? Spring already has one defined?
* BatchStrategy for RedisCacheManagerWriter? See "The cache implementation defaults to use KEYS and DEL to clear the cache." in Spring Redis docs
* Lettuce or Jedis for connection provider? (Probably Lettuce.)
* What is automatic failover? Sentinel?
* Write to Master, Read from Replica is best strategy? Probably. 

<b> References</b>
* https://docs.spring.io/spring-data/redis/reference/redis/redis-cache.html
* https://aws.amazon.com/blogs/architecture/data-caching-across-microservices-in-a-serverless-architecture/
* https://softwaremill.com/caches-in-microservice-architecture/

  
#### Design 
I have this many stitches for a project. 

### Row Counter 
#### Purpose 
Keep track of which line in a piece or section of a piece that you're working on. 

#### Design 
##### Overview 
* You press '-' or '+' and that increases the current row that you're on.
* Counts which row that you're on in the current section of your design.
* Would be nice to have it for each panel OR the entire garment. Most apps offer this as a premium feature. 

***** Design 
* Is a table connected to garments/garment_panels.
* 
### Timer 

## DB Objects
### Projects 
A project is one or more patterns that a user is working on. A project has: 

* name: custom identifier a user 
* patterns: a list of garments/knittables a user is working on.
* type: the kind of project that you're going to be working on. E.g., crochet, knitting, machine, loom. For now, we'll just do crochet. 
* start date: when the user began working on the project. 
* finish date: when the user wants to finish the project by. 

### Pattern 
Something you buy from another individual or create on your own. It is an external file/set of files that users can import. Patterns provide directions for how to create the piece(s) in a project. 

Users can upload the file from personal or external sources. For now, we'll just focus on from your device and Google Drive. Dropbox after if possible. 

Files are validated when a user attempts to upload them. 

Users can edit the pdf/write on it, highlight sections, add notes, etc.. 

Patterns have: 
* name
* files
* global counter
* one or more sections

### Sections 
* name
* Types of panels can be introduced later. E.g., sleeve, torso, cuff, etc. 

