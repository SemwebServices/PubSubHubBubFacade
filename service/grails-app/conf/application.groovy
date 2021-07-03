
grails.plugin.twitterbootstrap.fixtaglib = true

// Added by the Spring Security Core plugin:
grails.plugin.springsecurity.userLookup.userDomainClassName = 'feedfacade.User'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'feedfacade.UserRole'
grails.plugin.springsecurity.authority.className = 'feedfacade.Role'

grails.plugin.springsecurity.controllerAnnotations.staticRules = [
	[pattern: '/',                      access: ['permitAll']],
	[pattern: '/actuator/**',           access: ['ROLE_ADMIN', 'isFullyAuthenticated()']],
	[pattern: '/error',                 access: ['permitAll']],
	[pattern: '/setup/**',              access: ['permitAll']],
	[pattern: '/index',                 access: ['permitAll']],
	[pattern: '/index.gsp',             access: ['permitAll']],
	[pattern: '/shutdown',              access: ['permitAll']],
	[pattern: '/assets/**',             access: ['permitAll']],
	[pattern: '/home/**',               access: ['permitAll']],
	[pattern: '/hub/**',                access: ['permitAll']],
	[pattern: '/hubClient/**',          access: ['permitAll']],
	[pattern: '/sourcefeed/index',      access: ['permitAll']],
	[pattern: '/sourcefeed/feed',       access: ['permitAll']],
	[pattern: '/**/js/**',              access: ['permitAll']],
	[pattern: '/**/css/**',             access: ['permitAll']],
	[pattern: '/**/fonts/**',           access: ['permitAll']],
	[pattern: '/**/images/**',          access: ['permitAll']],
	[pattern: '/**/favicon.ico',        access: ['permitAll']]
]

grails.plugin.springsecurity.filterChain.chainMap = [
	[pattern: '/assets/**',      filters: 'none'],
	[pattern: '/**/js/**',       filters: 'none'],
	[pattern: '/**/css/**',      filters: 'none'],
	[pattern: '/**/images/**',   filters: 'none'],
	[pattern: '/**/favicon.ico', filters: 'none'],
	[pattern: '/**',             filters: 'JOINED_FILTERS']
]

grails.mime.use.accept.header = true


// Move the configuration to rabbit here - it appears that environment variable interpolation for
// maps within arrays has regressed to the behaviour seen in this issue:
// https://github.com/grails/grails-core/issues/10340
// Moving it here and using System.getenv('VAR')?:'default' instead
rabbitmq = [
  connections:[
    [
      name: 'localRMQ',
      host: System.getenv('RABBIT_HOST')?:'rabbitmq',
      username: System.getenv('CAP_RABBIT_USER')?:'cap',
      password: System.getenv('CAP_RABBIT_PASS')?:'cap',
      automaticReconnect: true,
      requestedHeartbeat: 120
    ]
  ]
]
