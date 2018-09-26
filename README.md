Rulex – A Blockchain Middleware
===
Although "decentralize everything" and/or "decentralize all the things" are viral buzzwords in blockchain world, our viewpoint of the blockchain ecosystem is a bit different.

In the understanding of most people, "decentralize everything" means that every decentralized app should be created specifically for blockchain from scratch, and its business logic should be fully or partially implemented as smart contracts, and as a result, through years of endeavor, in totally there are only less than 800 or so dApps has been developed on Ethereum, while only 4 ones have 500 and more daily active users.  

Comparing to the disappointing situation of dApps, there are more than 2.8M apps for Android, 2M for iOS, and 1M for WeChat, and more yet to come.  If we could convince their developers to “transit” some of them to dApps by putting a small part of their data, such as value data or credentials on blockchain, with contained or negligible efforts, then we should be able to cultivate a strong ecosystem for blockchain fairly quickly.  Given that a public blockchain can ensure the trustworthiness of such data, such transition can introduce a new user-attractive feature to their dApp, thereby improve the competitiveness, so we are very optimal over the future of this way of ecosystem cultivation.

To minimize the efforts of the developers who decide to port their apps to dApps, we have come out a solution to develop a middleware to ease the development work. Below is the architecture diagram of this middleware named Rulex:

![image](https://github.com/rulex-bmw/BMW/blob/master/picture/190254304704775365.png)

The key component of Rulex middleware is BSB (Blockchain Service Bus), it connects to underlying public blockchains through corresponding adaptors, accept incoming ledger status putting and retrieving requests from upper layers such as DBA (Direct Block Access), DSM (Domain Specific Model) and even applications, marshal them, order them, buffer them and when ready interact with adaptors to stream transited requests to blockchains in a secured, reliable and immutable manner. On the other side, when the responses are received from blockchain via adaptors, it will transit responses to upper-layer understandable formats and notify them to make further process.  Please notice that because the possible gap between the performance requirements from applications and relative lower TPS capability of a public blockchain, this request-response model is designed as asynchronous. To ensure the entire process is secured and reliable, some mature mechanisms and practices have also been borrowed to our design, e.g., embedded nosql database, chained with hash, etc.

The other important component is DSM, it enables developers to put parts of their data, vertically (a subset of fields) and horizontally (a subset of records) to blockchain with largely a configuration file with the help of very small snippets of codes.

The DBA module Is to enable the front-end application regardless of mobile apps or web ones to access the BSB service via JSON-RPC or gRPC (i.e., encoded in protocol buffer format), which is very useful for an app that has very simple backend logic to migrate to blockchain, a.k.a., make it blockchain ready.


