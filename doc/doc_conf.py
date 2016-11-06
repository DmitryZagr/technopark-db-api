DISCR = {
	'forum': {
		'fields': {
			'user': '```str``` founder email',
			'shortName': '```str``` forum slug',
			'name': '```str``` forum name',
			'order': "```str``` sort order (by date). Possible values: ```['desc', 'asc']```. Default: 'desc'",
			'limit': "```int``` return limit",
			'forum': '```str``` forum shortName',
			'since_id': "```int``` return entities in interval [since_id, max_id]",
			'since': "```str``` include forums created since date. Format: 'YYYY-MM-DD hh-mm-ss'",
			'related': "```array``` include related entities. Possible values: ```['user',]```. Default: []",
		},
		'methods': {
			'create': 'Create new forum',
			'details': 'Get forum details',
			'listPosts': 'Get posts from this forum',
			'listThreads': 'Get threads from this forum',
			'listUsers': 'Get user with posts on this forum',
		}
	},
	'post': {
		'fields': {
			'user': '```str``` author email',
			'order': "```str``` sort order (by date). Possible values: ```['desc', 'asc']```. Default: 'desc'",
			'thread': '```int``` thread id of this post',
			'post': '```int``` post id',
			'parent': '```int``` id of parent post. Default: None',
			'forum': '```str``` forum shortName',
			'message': '```str``` post body',
			'vote': "```int``` likes/dislikes. Possible values: [1, -1]",
			'date': "```str``` date of creation. Format: 'YYYY-MM-DD hh-mm-ss'",
			'since': "```str``` include posts created since date. Format: 'YYYY-MM-DD hh-mm-ss'",
			'related': "```array``` include related entities. Possible values: ```['user', 'thread', 'forum']```. Default: []",
			'limit': "```int``` return limit",
			'isSpam': "```bool``` is post marked as spam",
			'isEdited': "```bool``` is post marked as edited",
			'isDeleted': "```bool``` is post marked as deleted",
			'isHighlighted': "```bool``` is post marked as higlighted",
			'isApproved': "```bool``` is post marked as approved by moderator",
		},
		'methods': {
			'list': 'List posts',
			'create': 'Create new post',
			'details': 'Get post details',
			'remove': 'Mark post as removed',
			'restore': 'Cancel removal',
			'update': 'Edit post',
			'vote': 'likes/dislikes post',
		}
	},
	'user': {
		'fields': {
			'since': "```str``` include posts from this user created since date. Format: 'YYYY-MM-DD hh-mm-ss'",
			'order': "```str``` sort order (by name). Possible values: ```['desc', 'asc']```. Default: 'desc'",
			'user': '```str``` user email',
			'username': '```str``` user name',
			'follower': '```str``` follower email',
			'followee': '```str``` followee email',
			'name': '```str``` user name',
			'limit': "```int``` return limit",
			'since_id': "```int``` return entities in interval [since_id, max_id]",
			'email': '```str``` user email',
			'isAnonymous': "```bool``` is user marked as anonymous",
			'about': "```str``` user info",
		},
		'methods': {
			'create': 'Create new user',
			'details': 'Get user details',
			'follow': 'Mark one user as folowing other user',
			'unfollow': 'Mark one user as not folowing other user anymore',
			'listPosts': 'Get posts from this user',
			'updateProfile': 'Update profile',
			'listFollowers': 'Get followers of this user',
			'listFollowing': 'Get followees of this user',
		}
	},
	'thread': {
		'fields': {
			'thread': '```int``` thread id of this post',
			'isDeleted': "```bool``` is thread marked as deleted",
			'isClosed': "```bool``` is thread marked as closed",
			'message': '```str``` thread message',
			'user': '```str``` founder email',
			'date': "```str``` date of creation. Format: 'YYYY-MM-DD hh-mm-ss'",
			'slug': "```str``` thread slug",
			'title': "```str``` thread title",
			'limit': "```int``` return limit",
			'forum': '```str``` parent forum shortName',
			'related': "```array``` include related entities. Possible values: ```['user', 'forum']```. Default: []",
			'since': "```str``` include threads created since date. Format: 'YYYY-MM-DD hh-mm-ss'",
			'order': "```str``` sort order (by date). Possible values: ```['desc', 'asc']```. Default: 'desc'",
			'vote': "```int``` likes/dislikes. Possible values: [1, -1]",
		},
		'methods': {
			'list': 'List threads',
			'create': 'Create new thread',
			'details': 'Get thread details',
			'remove': 'Mark thread as removed',
			'open': 'Mark thread as opened',
			'close': 'Mark thread as closed',
			'restore': 'Cancel removal',
			'listPosts': 'Get posts from this thread',
			'update': 'Edit thread',
			'subscribe': 'Subscribe user to this thread',
			'unsubscribe': 'Unsubscribe user from this thread',
			'vote': 'likes/dislikes thread',
		}
	},
}