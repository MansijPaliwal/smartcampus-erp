# Code Efficiency, Resilience, and Production Standards Rules

## 1. Mandatory Library & Framework Utilization
- Never implement custom helper utilities (e.g., custom token generators, manual JSON parsers, basic file-io streams, custom string helpers). Use robust, battle-tested options like Apache Commons, Guava, Jackson, or standard JDK 17+ / framework-native equivalents.
- Leverage the host framework's built-in ecosystem completely (e.g., if in Spring Boot, use Spring AI abstractions, standard Validation starters, Spring Data JPA internal query derivations, and native Spring event pub-sub mechanisms).

## 2. Performance & Algorithmic Efficiency
- Every custom algorithm or data manipulation layer must be analyzed for time and space complexity. Optimize for O(N) or O(log N) operations wherever possible. Avoid nested loops, N+1 query problems in the database layer, and blocking I/O calls on main execution threads.
- Utilize memory-efficient structures. For caching, token blacklisting, or frequency checks, utilize proper data-store features (like native Redis TTLs, sets, or hashes) instead of relying heavily on in-memory application collections.

## 3. Robust Error Wrapping & Defensibility
- Do not wrap code blocks in generic "catch (Exception e)" blankets that swallow real errors. Implement granular, type-specific exception handling.
- All integrations with third-party webhooks or external APIs must include defensive resilience layers: configure tight connection/read timeouts, implement exponential backoffs, and ensure the UI layer always receives a structural fallback or proper HTTP status if a network barrier is hit.
- Use strict data-concurrency levels (e.g., proper database isolation levels or transactional definitions) for highly sensitive ledger or state modifications.

## 4. Clean Code Over Extra Word Count
- Prioritize clean, idiomatic code over verbose output. Skip unnecessary textual fluff, and provide clean, modular, and fully populated classes/methods rather than leaving generic comments like "// implement here".
