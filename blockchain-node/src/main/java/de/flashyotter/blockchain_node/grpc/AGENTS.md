Implements the gRPC services defined in `src/main/proto`.

- `ChainGrpcService`, `MiningGrpcService`, `WalletGrpcService` delegate to the corresponding services.
- `GrpcMapper` converts between protobuf messages and domain objects.
