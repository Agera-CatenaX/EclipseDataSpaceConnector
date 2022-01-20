## Consumer

API interactions
- initiate contract negotiation
- initiate transfer process 

Possible solution:
Expose a generic API

Questions:
Is contract negotiation an internal step or should be triggered by a client? 

## Provider

Extension point:
- trigger data transfer (FileTransferDataStreamPublisher)

Possible solution:
- Provide extensions to the most common use cases that can be configured without java
- Provide generic extensions that call an external process to handle the data transfer (or any other task) 

API interactions:
- register assets and policies

Possible solution:
Expose a generic API

