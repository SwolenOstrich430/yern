// package com.yern.controller.user;

// final class WithUserDetailsSecurityContextFactory
// 		implements WithSecurityContextFactory<WithUserDetails> {

// 	private final UserDetailsService userDetailsService;

// 	@Autowired
// 	public WithUserDetailsSecurityContextFactory(UserDetailsService userDetailsService) {
// 		this.userDetailsService = userDetailsService;
// 	}

// 	public SecurityContext createSecurityContext(WithUserDetails withUser) {
// 		String username = withUser.value();
// 		Assert.hasLength(username, "value() must be non-empty String");
// 		UserDetails principal = userDetailsService.loadUserByUsername(username);
// 		Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(principal, principal.getPassword(), principal.getAuthorities());
// 		SecurityContext context = SecurityContextHolder.createEmptyContext();
// 		context.setAuthentication(authentication);
// 		return context;
// 	}
// }