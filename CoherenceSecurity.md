## Relevant classes ##
All of these hooks can be used for audit as well as security

| Name | Description | Example | Signature |
|:-----|:------------|:--------|:----------|
| IdentityTransformer | Client transforms the Subject -> Token (may be null) |  Gridman Kerberos | Object transformIdentity(Subject subject) |
| IdentityAsserter | Proxy transforms the Token -> Subject (may be null) | Gridman Kerberos | Subject assertIdentity(Object oToken) |
| CacheServiceProxy | Intercepts cache requests | Gridman Simple | NamedCache ensureCache(String cache, ClassLoader classLoader) |
| InvokeServiceProxy | Intercepts invoke requests | Gridman Simple | execute + query methods |


Gridman provides the following :

| Name | Description | Example | Signature |
|:-----|:------------|:--------|:----------|
| CacheSecurityProvider | Simple interface for caches | Use a cache or AD Groups | boolean checkAccess (Subject subject, String cacheName, boolean readOnly) |
| InvokeSecurityProvider | Simple interface for invocable | Use a cache or AD Groups | boolean checkInvocation(Subject subject, Invocable invocable) |
| PermissionedNamedCache | Wrapper named cache to provide read/write functionality | Could extend to provide object level security | -         |
|AuthorizedHostFilter | A filter to restrict cluster membership | Cache or database | boolean evaluate(Object iNetAddress) |

### Cache level session security ###

Our solution provides :

  * Cache level security - you can read/write an entire cache.
  * Session level - The Asserter/Transformer are only called once per session (actually they are called 3 times - socket / channel / cq.  However, the cost is low.

### Object level security ###
This could be provided using a WrapperCache.  However, remember Coherence is a **high performance** product so you better make sure this is a fast call!