# Free Lunch

[ClojuTRE Video](https://www.youtube.com/watch?v=Bi7gvzJ_4OE)

[Slides](https://github.com/DomKM/ethereum-free-lunch/raw/master/slides.pdf)

[GraphQL clojureD Video](https://www.youtube.com/watch?v=sFUd-CtnJv8) (mentioned in video and slides above)

Based on [MemeFactory](https://github.com/district0x/memefactory).

## Install

### [Leiningen](https://leiningen.org/)

```
brew install leiningen
```

### [Node](https://nodejs.org/)

```
brew install node
```

### [Ganache](https://truffleframework.com/ganache)

```
npm install -g ganache-cli
```

### [IPFS](https://ipfs.io/)

```
brew install ipfs
```

### [Solidity](https://github.com/ethereum/solidity)

```
brew install solidity
```

## Develop

### Ganache

```
ganache-cli -p 8549
```

### IPFS

```
ipfs daemon
```

### Solidity

```
lein solc auto
```

### FreeLunch Server

```
lein repl
```

```clj
(start-server!)
```

### FreeLunch UI

```
lein repl
```

```clj
(start-ui!)
```

Open http://localhost:4598/.
