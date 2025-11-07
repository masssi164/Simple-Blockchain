# GitHub Issues - Quick Reference

This document provides templates for creating GitHub issues for the remaining high-priority problems.

---

## Issue Template #1: Missing Comprehensive Logging

**Title:** Add comprehensive logging to critical code paths

**Labels:** `enhancement`, `observability`

**Description:**
Many critical operations lack structured logging, making it difficult to debug production issues and track system behavior.

**Missing logging in:**
- Chain re-organizations (only partial logging exists)
- Mempool transaction add/remove operations
- Wallet operations (no audit trail)
- P2P sync failures (only warnings)

**Proposed Solution:**
Add comprehensive structured logging with:
- MDC (Mapped Diagnostic Context) for correlation IDs
- Consistent log levels (DEBUG, INFO, WARN, ERROR)
- Performance metrics at key points
- Audit trail for wallet operations

**Example:**
```java
@Slf4j
public class Chain {
    private void switchActiveChain(String newTipHash) {
        log.info("Starting chain re-org to tip {}", newTipHash);
        // ... existing code
        log.info("Re-org complete: {} blocks, {} txs, new height {}", 
                 branchRev.size(), totalTxs, getLatest().getHeight());
    }
}
```

**Acceptance Criteria:**
- [ ] All state-changing operations are logged
- [ ] MDC correlation IDs added to requests
- [ ] Performance metrics added for slow operations
- [ ] Wallet operations have audit trail

**Priority:** High

---

## Issue Template #2: Inefficient UTXO Computation

**Title:** Optimize currentUtxoIncludingPending() for better performance

**Labels:** `performance`, `optimization`

**Description:**
The `NodeService.currentUtxoIncludingPending()` method recalculates the entire UTXO set on every call by copying confirmed UTXOs and applying all pending transactions. This is called on every API request to `/api/wallet`.

**Performance Impact:**
- O(n) complexity where n = mempool size
- Deep copy of entire UTXO map on each call
- High CPU and memory usage with large mempool
- Unnecessary repeated computation

**Current Code:**
```java
public Map<String, TxOutput> currentUtxoIncludingPending() {
    Map<String, TxOutput> effective = new HashMap<>(chain.getUtxoSnapshot());
    List<Transaction> pending = mempool.take(Integer.MAX_VALUE);
    for (Transaction tx : pending) {
        // ... modify effective
    }
    return effective;
}
```

**Proposed Solutions:**

**Option 1: Caching**
- Cache the computed result
- Invalidate on new transaction or block
- Add cache metrics

**Option 2: Reactive Incremental View**
- Maintain combined view using reactive streams
- Update incrementally on changes
- Subscribe to mempool and chain events

**Acceptance Criteria:**
- [ ] UTXO computation reduced to O(1) or O(log n) for repeated calls
- [ ] Memory usage reduced
- [ ] Cache invalidation tested
- [ ] Performance benchmarks show improvement

**Priority:** Medium-High

---

## Issue Template #3: Add React Error Boundaries

**Title:** Implement Error Boundaries for React components

**Labels:** `frontend`, `bug`, `user-experience`

**Description:**
The React application has no Error Boundaries. If any component throws an error, the entire application crashes with a blank white screen, providing no feedback to users.

**Impact:**
- Poor user experience
- No error recovery mechanism
- Lost user work
- No error reporting to monitoring service

**Proposed Solution:**
Create Error Boundary component and wrap main sections:

```typescript
class ErrorBoundary extends React.Component<Props, State> {
    static getDerivedStateFromError(error: Error) {
        return { hasError: true, error };
    }
    
    componentDidCatch(error: Error, errorInfo: ErrorInfo) {
        console.error('Error caught by boundary:', error, errorInfo);
        // Send to error reporting service (e.g., Sentry)
    }
    
    render() {
        if (this.state.hasError) {
            return <ErrorFallback error={this.state.error} 
                                  onReset={this.resetError} />;
        }
        return this.props.children;
    }
}
```

**Locations to wrap:**
- Dashboard component
- WalletView component
- MiningArea component
- BlockList component

**Acceptance Criteria:**
- [ ] Error Boundary component created
- [ ] Main sections wrapped with Error Boundary
- [ ] Error fallback UI designed and implemented
- [ ] Errors logged to console and monitoring service
- [ ] User can recover from errors (reset button)
- [ ] Tests added for error scenarios

**Priority:** High

---

## Issue Template #4: WebSocket Reconnection Strategy Improvements

**Title:** Improve WebSocket reconnection strategy and user feedback

**Labels:** `frontend`, `enhancement`, `networking`

**Description:**
The WebSocket reconnection mechanism has unlimited retries with exponential backoff but provides no user feedback about connection status.

**Current Issues:**
- No maximum retry limit
- No user notification of connection status
- Silent failures during extended outages
- Battery drain from constant retries on mobile
- Stale data displayed without indication

**Current Code:**
```typescript
private scheduleReconnect() {
    this.ws = undefined;
    setTimeout(() => this.open(), this.reconnectMs);
    this.reconnectMs = Math.min(this.reconnectMs * 2, 30000);
}
```

**Proposed Solution:**

**1. Connection State Management**
```typescript
enum ConnectionState {
    CONNECTED = 'CONNECTED',
    CONNECTING = 'CONNECTING',
    DISCONNECTED = 'DISCONNECTED',
    ERROR = 'ERROR'
}
```

**2. React Context for State**
```typescript
export const WebSocketContext = React.createContext<{
    state: ConnectionState;
    retryCount: number;
}>({ state: ConnectionState.DISCONNECTED, retryCount: 0 });
```

**3. UI Indicator**
- Show connection status indicator in header
- Display retry count and next retry time
- Provide manual reconnect button
- Show warning when offline

**4. Retry Limits**
- Maximum 10 retry attempts
- After max attempts, require user action
- Reset retry count on successful connection

**Acceptance Criteria:**
- [ ] Connection state enum implemented
- [ ] React context provides connection state
- [ ] UI indicator shows connection status
- [ ] Maximum retry limit enforced
- [ ] User can manually trigger reconnect
- [ ] Retry count resets on success
- [ ] Tests added for reconnection scenarios

**Priority:** Medium

---

## Issue Template #5: Implement Rate Limiting

**Title:** Add rate limiting to API endpoints

**Labels:** `security`, `enhancement`, `api`

**Description:**
No rate limiting is implemented on any API endpoint, creating DoS vulnerability and allowing unfair resource usage.

**Vulnerable Endpoints:**
- `/api/wallet/send` - Transaction submission
- `/api/mining/mine` - Mining requests
- `/api/chain/**` - All chain queries
- WebSocket connections and messages

**Security Impact:**
- DoS attack vector
- Resource exhaustion
- Unfair use of shared mining resources
- Spam transactions in mempool

**Proposed Solution:**

Use Resilience4j or Bucket4j for rate limiting:

```java
@Configuration
public class RateLimitConfig {
    
    @Bean
    public RateLimiter apiRateLimiter() {
        return RateLimiter.create(100, Duration.ofMinutes(1));
    }
    
    @Bean
    public RateLimiter miningRateLimiter() {
        return RateLimiter.create(10, Duration.ofMinutes(1));
    }
}
```

**Rate Limit Recommendations:**
- General API: 100 requests/minute per IP
- Mining: 10 requests/minute per IP
- Transaction submission: 20 requests/minute per user
- WebSocket messages: 50 messages/minute per connection

**Headers to Return:**
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 87
X-RateLimit-Reset: 1609459200
```

**Acceptance Criteria:**
- [ ] Rate limiting library integrated
- [ ] Limits applied to all endpoints
- [ ] Different limits for different endpoint types
- [ ] Rate limit headers returned in responses
- [ ] 429 Too Many Requests status code returned when exceeded
- [ ] Rate limits configurable via properties
- [ ] Monitoring/metrics for rate limit hits
- [ ] Tests for rate limiting behavior

**Priority:** High

---

## Issue Template #6: Security - JWT Secret Management

**Title:** Improve JWT secret management and remove from config files

**Labels:** `security`, `critical`

**Description:**
JWT secrets are currently stored in `.env` files and documented in `README.md`, creating security risks.

**Current Issues:**
```
NODE_JWT_SECRET=myTopSecret
VITE_NODE_JWT_SECRET=myTopSecret
```

**Security Risks:**
- Secrets may be committed to version control
- Same secret used across all environments
- Secrets visible in configuration files
- No secret rotation mechanism

**Proposed Solution:**

**1. Remove Secrets from Config**
- Remove from `.env` examples
- Add `.env` to `.gitignore` (already present)
- Update README to explain secret generation

**2. Generate Secrets at Startup**
```java
@Configuration
public class JwtConfig {
    
    @Bean
    public String jwtSecret(@Value("${jwt.secret:}") String configSecret) {
        if (configSecret.isBlank()) {
            String generated = generateSecureSecret();
            log.warn("No JWT secret configured, using generated secret. " +
                     "Set jwt.secret property for production use.");
            return generated;
        }
        return configSecret;
    }
    
    private String generateSecureSecret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
```

**3. External Secrets Management** (Production)
- HashiCorp Vault integration
- AWS Secrets Manager
- Azure Key Vault
- Environment variables injected at deployment

**4. Secret Rotation**
- Support multiple active secrets
- Gradual rotation without downtime
- Audit trail for secret changes

**Acceptance Criteria:**
- [ ] Secrets removed from `.env` and README examples
- [ ] Secure secret generation at startup if not provided
- [ ] Documentation for production secret management
- [ ] Support for external secrets management
- [ ] Different secrets per environment enforced
- [ ] Secret rotation mechanism designed
- [ ] Security audit performed

**Priority:** Critical

---

## Issue Template #7: Add Comprehensive Unit Tests

**Title:** Increase test coverage for critical components

**Labels:** `testing`, `quality`

**Description:**
Many critical components lack unit tests, increasing the risk of regressions and making refactoring dangerous.

**Components Missing Tests:**
- `NodeService` UTXO calculation logic
- `MempoolService` fee market and eviction
- `SyncService` retry logic and error handling
- `WalletService` transaction creation
- `Chain` re-organization logic (partial coverage)

**Current Coverage:** Unknown (no coverage reports)
**Target Coverage:** 80%+

**Testing Strategy:**

**1. Unit Tests**
```java
@Test
void shouldCalculateUtxoIncludingPending() {
    // Given: confirmed UTXO and pending transactions
    Map<String, TxOutput> confirmed = createConfirmedUtxo();
    Transaction pending = createPendingTx();
    
    // When: calculating UTXO with pending
    Map<String, TxOutput> result = service.currentUtxoIncludingPending();
    
    // Then: pending inputs removed, outputs added
    assertThat(result).containsKeys(expectedOutputs);
    assertThat(result).doesNotContainKeys(spentInputs);
}
```

**2. Integration Tests**
- Use TestContainers for database tests
- Test full transaction flow end-to-end
- Test chain re-organizations
- Test P2P sync scenarios

**3. Property-Based Tests**
- Use JUnit QuickCheck or jqwik
- Test consensus rules with random inputs
- Test cryptographic operations

**4. Test Categories**
- Unit tests: Fast, isolated
- Integration tests: Database, network
- End-to-end tests: Full stack

**Tools:**
- JUnit 5
- Mockito
- TestContainers
- JaCoCo for coverage reports
- ArchUnit for architecture tests

**Acceptance Criteria:**
- [ ] 80%+ code coverage achieved
- [ ] All critical business logic has unit tests
- [ ] Integration tests for database operations
- [ ] End-to-end tests for user flows
- [ ] Coverage reports in CI pipeline
- [ ] Tests run fast (<2 minutes for unit tests)
- [ ] Parameterized tests for edge cases

**Priority:** High

---

## Summary Table

| # | Title | Labels | Priority | Effort |
|---|-------|--------|----------|--------|
| 1 | Add comprehensive logging | enhancement, observability | High | Medium |
| 2 | Optimize UTXO computation | performance, optimization | Medium-High | Medium |
| 3 | Add React Error Boundaries | frontend, bug, UX | High | Low |
| 4 | Improve WebSocket reconnection | frontend, enhancement | Medium | Medium |
| 5 | Implement rate limiting | security, enhancement | High | Medium |
| 6 | JWT secret management | security, critical | Critical | Low |
| 7 | Add comprehensive tests | testing, quality | High | High |

---

## Creating Issues on GitHub

To create these issues on GitHub:

1. Go to https://github.com/masssi164/Simple-Blockchain/issues/new
2. Copy the title and description from above
3. Add the suggested labels
4. Assign priority label (critical, high, medium, low)
5. Add to appropriate milestone if exists
6. Assign to team members as appropriate

## Issue Tracking

Once issues are created, reference them in commits:
```
git commit -m "Fix rate limiting (#5)"
```

And in pull requests:
```
Closes #5
Fixes #6
Relates to #7
```

