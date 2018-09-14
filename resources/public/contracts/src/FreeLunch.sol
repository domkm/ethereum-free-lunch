pragma solidity ^0.4.24;

contract FreeLunch {
  
  mapping (string => bytes) freebies;
  
  event Freebie(string);
  
  function setFreebie(string k, bytes dataHash) public {
    freebies[k] = dataHash;
    emit Freebie(k);
  }
  
  function loadFreebie(string k) public view returns (string, bytes) {
    return (k, freebies[k]);
  }

}
