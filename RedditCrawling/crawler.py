import praw
from elasticsearch import Elasticsearch
import json
import pprint
from bisect import bisect_left
from copy import deepcopy
import math
from scipy.special import entr
from numpy.linalg import norm
import numpy as np

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
user_timestamps = dict()

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

first_comment_timestamp = None
last_comment_timestamp = None
L_d = None
L_m = None
L_w = None

def add_timestamp(user_id, comment_timestamp):
    global first_comment_timestamp
    global last_comment_timestamp

    if not user_id in user_timestamps:
        user_timestamps[user_id] = []

    user_list = user_timestamps[user_id]
    user_list.append(comment_timestamp)
    user_timestamps[user_id] = user_list

    if first_comment_timestamp is None:
        first_comment_timestamp = comment_timestamp
        last_comment_timestamp = comment_timestamp
    else:
        first_comment_timestamp = min(first_comment_timestamp, comment_timestamp)
        last_comment_timestamp = max(last_comment_timestamp, comment_timestamp)

def F(user, W, x):
    user_list = user_timestamps[user]
    to_search = W * x
    index = bisect_left(user_list, to_search)

    if (index == len(user_list)):
        return 0
    elif user_list[index] <= (W * x + W):
        return 1

    return 0

def D(user, h):
    total = 0

    for i in range(L_d):
        total += F(user, 60, 24 * i + h)

    return total

def W(user, d):
    total = 0

    for i in range(L_w):
        total += F(user, 60 * 24, 7 * i + d)

    return total

def compute_time_measures(user):
    user_d = []
    user_w = []

    for h in range(24):
        user_d.append(D(user, h))
    for d in range(7):
        user_w.append(W(user, d))

    max_d = max(user_d)
    max_w = max(user_w)
    total_d = sum(user_d)
    total_w = sum(user_w)
    normalised_d = deepcopy(user_d)
    normalised_w = deepcopy(user_w)

    if total_d != 0:
        normalised_d = list(map(lambda x: float(x) / total_d, user_d))
    if total_w != 0:
        normalised_w = list(map(lambda x: float(x) / total_w, user_w))

    e_d = 0
    for h in range(24):
        if normalised_d[h] != 0:
            e_d += (normalised_d[h] * math.log(normalised_d[h]))

    e_w = 0
    for d in range(7):
        if normalised_w[d] != 0:
            e_w += (normalised_w[d] * math.log(normalised_w[d]))

    PDH = (math.log(24) - e_d) * max_d
    PWD = (math.log(7) - e_w) * max_w

    return PDH, PWD

def P(user, d, k):
    total = 0

    for i in range(24):
        total += F(user, 60, 24 * (d + 7 * k) + i)

    return total

def get_profile(user, k):
    return [P(user, d, k) for d in range(7)]

def active(user, k):
    profile = get_profile(user, k)
    active_days = []

    for d in range(7):
        if profile[d] != 0:
            active_days.append(d)

    return active_days

def JSD(P, Q):
    _P, _Q = np.array(P), np.array(Q)
    sum_p = sum(P)
    sum_q = sum(Q)

    if sum_p != 0:
        _P = _P / sum_p
    if sum_q != 0:
        _Q = _Q / sum_q

    return entr(0.5 * (_P + _Q)).sum() - 0.5 * (entr(_P).sum() + entr(_Q).sum())

def similarity_1(user, i, j):
    active_i = active(user, i)
    active_j = active(user, j)
    active_common = list(set(active_i) & set(active_j))

    max_len = max(len(active_i), len(active_j))
    if max_len == 0:
        return 0

    return float(len(active_common)) / max_len

def similarity_2(user, i, j):
    profile_i = get_profile(user, i)
    profile_j = get_profile(user, j)

    return 1 - (JSD(profile_i, profile_j) / math.log(2))

def similarity_3(user, i, j):
    active_i = active(user, i)
    active_j = active(user, j)
    active_all = list(set(active_i) | set(active_j))

    if len(active_all) == 0:
        return 0

    total = 0
    for d in range(7):
        p_i = P(user, d, i)
        p_j = P(user, d, j)

        if (p_i + p_j) != 0:
            total += math.pow(float(p_i - p_j) / (p_i + p_j), 2)

    return 1 - float(total) / len(active_all)

def compute_similarities(user):
    ws1_total = 0
    ws2_total = 0
    ws3_total = 0
    count = 0

    for i in range(L_w):
        for j in range(i + 1, L_w):
            count += 1
            ws1_total += similarity_1(user, i, j)
            ws2_total += similarity_2(user, i, j)
            ws3_total += similarity_3(user, i, j)

    if count == 0:
        count = 1

    return float(ws1_total) / count, float(ws2_total) / count, float(ws3_total) / count

def process_timestamps():
    global L_d
    global L_m
    global L_w
    difference = last_comment_timestamp - first_comment_timestamp
    L_d = int(float(difference) / (60 * 60 * 24))
    L_m = int(float(difference) / (60))
    L_w = int(float(difference) / (60 * 60 * 24 * 7))

    for user in user_timestamps:
        user_list = user_timestamps[user]
        user_list = list(map(lambda t: int((t - first_comment_timestamp) / 60), user_list))
        user_list.sort()

        user_timestamps[user] = user_list

    print(first_comment_timestamp)
    print(last_comment_timestamp)
    print(L_m)
    print(L_d)
    print(L_w)

    regularity_file = open('./regularity', 'w')
    regularity_file.write('user,PDH,PWD,WS1,WS2,WS3\n')

    for user in user_timestamps:
        print(user_timestamps[user])
        PDH, PWD = compute_time_measures(user)
        regularity_file.write(user + ',' + str(round(PDH, 2)) + ',' + str(round(PWD, 2)))
        WS1, WS2, WS3 = compute_similarities(user)
        regularity_file.write(',' + str(round(WS1, 2)) + ',' + str(round(WS2, 2)) + ',' + str(round(WS3, 3)) + '\n')

    regularity_file.close()
    

for submission in reddit.subreddit(subreddit_name).hot(limit=15):

        participants = set()
        submission_id = submission.id

        comment_mapping = dict()

        comment_id = convert_id_to_integer(comment_mapping, submission_id)

        thread_json = {}
        comment_list = []

        submission.comments.replace_more(limit=None)
        all_comments = submission.comments.list()

        #print(vars(submission))
        comment_json = {
                'nickname': submission.author_fullname,
                'genid': comment_id,
                'time': int(submission.created_utc),
                'text': submission.selftext
        }
        #print(comment_json)
        #exit(0)
        #comment_list.append(comment_json)

        for comment in all_comments:
                try:
                        author_id = comment.author_fullname

                        update_user_rank(author_id, float(comment.ups))
                        update_user_rank(author_id, float(comment.downs) / 2)

                        comment_timestamp = int(comment.created_utc)
                        add_timestamp(author_id, comment_timestamp)

                        comment_id = convert_id_to_integer(comment_mapping, comment.id)
                        
                        parent_id = convert_id_to_integer(comment_mapping, get_parent_id(comment))
                        
                        comment_body = comment.body

                        participants.add(author_id)

                        comment_json = {
                                'nickname': author_id,
                                'genid': comment_id,
                                'refid': parent_id,
                                'time': comment_timestamp,
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

        #thread_json['is_rank'] = False
        #f = open('./thread_' + str(i) + '.json', 'w')
        #pprint.pprint(thread_json, stream=f)
        #f.close()
        
        #i = i + 1

        #if (len(comment_list) != 0):
        res = es.index(index=community_index.lower(), doc_type='thread', body=thread_json)
        #print(res['result'])

rankings = open('./rankings', 'w')
pprint.pprint(user_rank, stream=rankings)
rankings.close()

rank_document = {
        'user_rank': user_rank,
        'is_rank': True
}
#res = es.index(index=community_index, size=1000, doc_type='thread', body=rank_document)
#print(res['result'])

#for user in user_timestamps:
#    print(user_timestamps[user]
#process_timestamps()

#print(first_commment_timestamp)
#print(last_comment_timestamp)

exit(0)

res = es.search(index=community_index.lower(), size=1000, body={
        'query': {
                'match_all': {}
        }
})


threads = res['hits']['hits']

print(len(threads))

#for thread in threads:
#       print(thread['_source'])
