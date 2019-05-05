import praw
from elasticsearch import Elasticsearch
import json
import pprint

subreddit_name = 'DemocracyExperiment'
elasticsearch_host = 'localhost'
elasticsearch_port = '9200'
community_index = 'community_' + subreddit_name

reddit = praw.Reddit(client_id='kXX7BFBvklJ-0A',
                     client_secret='ZnN1x4vIsoLHa2oxA3e2lxpD0bI',
                     user_agent='/u/CataFetoiu')

es = Elasticsearch([{'host': elasticsearch_host, 'port': elasticsearch_port}])

print(reddit.read_only)  # Output: True
# TODO maybe reverse index from person to comments

user_rank = dict()

def get_parent_id(comment):
	parent_id = comment.parent_id
	delimiter_index = parent_id.find('_')

	return parent_id[(delimiter_index + 1):]

def convert_id_to_integer(mapping, comment_hash):
	if not comment_hash in mapping:
		size = len(mapping)
		mapping[comment_hash] = size

	return mapping[comment_hash]

def update_user_rank(user_id, score):
	if not user_id in user_rank:
		user_rank[user_id] = 0

	user_rank[user_id] = user_rank[user_id] + score


for submission in reddit.subreddit(subreddit_name).new(limit=20):

	participants = set()
	submission_id = submission.id

	comment_mapping = dict()

	convert_id_to_integer(comment_mapping, submission_id)

	thread_json = {}
	comment_list = []

	submission.comments.replace_more(limit=None)
	all_comments = submission.comments.list()

	for comment in all_comments:
		try:
			author_id = comment.author_fullname

			update_user_rank(author_id, float(comment.ups))
			update_user_rank(author_id, float(comment.downs) / 2)

			comment_timestamp = comment.created_utc
			
			comment_id = convert_id_to_integer(comment_mapping, comment.id)
			
			parent_id = convert_id_to_integer(comment_mapping, get_parent_id(comment))
			
			comment_body = comment.body

			participants.add(author_id)

			comment_json = {
				'nickname': author_id,
				'genid': comment_id,
				'refid': parent_id,
				'time': int(comment_timestamp),
				'text': comment_body
			}

			comment_list.append(comment_json)
			#pprint.pprint(comment_json)
		except:
			#print("no author name [deleted]")
                        a = 0

	thread_json['participants'] = list(participants)
	thread_json['body'] = comment_list
	thread_json['title'] = submission.title
	thread_json['is_rank'] = False

	pprint.pprint(thread_json)
	#if (len(comment_list) != 0):
	res = es.index(index=community_index.lower(), doc_type='thread', body=thread_json)
	print(res['result'])

rankings = open('./rankings', 'w')
pprint.pprint(user_rank, stream=rankings)
rankings.close()

rank_document = {
	'user_rank': user_rank,
	'is_rank': True
}
#res = es.index(index=community_index, doc_type='thread', body=rank_document)
#print(res['result'])

res = es.search(index=community_index.lower(), body={
	'query': {
		'match_all': {}
	}
})

threads = res['hits']['hits']

print(len(threads))
#for thread in threads:
#	print(thread['_source'])
