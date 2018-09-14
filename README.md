# Free Lunch

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
