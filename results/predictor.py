import numpy as np
from sklearn.svm import SVR
from sklearn.cluster import KMeans
from sklearn.model_selection import train_test_split
from sklearn.metrics import r2_score, mean_squared_error
import json, pprint
from copy import deepcopy
import math
import pandas as pd
from keras.models import Sequential
from keras.layers.core import Activation
from keras.layers.core import Dropout
from keras.layers.core import Dense
from keras.models import Model
from keras.optimizers import Adam
import tensorflow as tf

def split_by_range(rank_map):
    ranks = deepcopy(rank_map)
    values = list(ranks.values())

    min_value = min(values)
    max_value = max(values)
    interval = max_value - min_value

    for user in ranks:
        rank = ranks[user]
       
        if rank < min_value + (interval / 4):
            ranks[user] = 4
        elif rank < min_value + 2 * (interval / 4):
            ranks[user] = 3
        elif rank < min_value + 3 * (interval / 4):
            ranks[user] = 2
        else:
            ranks[user] = 1

    return ranks

def split_by_position(rank_map):
    ranks = deepcopy(rank_map)
    ranks_sorted = [(user, ranks[user]) for user in sorted(ranks, key=ranks.get, reverse=False)]
    size = len(ranks)

    for index in range(size):
        user = ranks_sorted[index][0]

        if index < (size / 4):
            ranks[user] = 4
        elif index < 2 * (size / 4):
            ranks[user] = 3
        elif index < 3 * (size / 4):
            ranks[user] = 2
        else:
            ranks[user] = 1

    return ranks

def log_ranks(rank_map):
    ranks = deepcopy(rank_map)

    for user in ranks:
        if ranks[user] != 0:
            ranks[user] = math.log(ranks[user])

    return ranks

def extract_columns(csv_file, user_column, feature_list):
    df = pd.read_csv(csv_file, sep=',')
    df = df.sort_values(by=[user_column], ascending=True)

    df = df[feature_list]
    return df

def create_labels_df(rank_map):
    df = pd.DataFrame(rank_map.items(), columns=['user', 'label'])
    df.sort_values(by=['user'], ascending=True)

    return df['label']

def svr_prediction(features, labels):
    features_train, features_test, labels_train, labels_test = train_test_split(
            features, labels, test_size=0.2, random_state=42)

    clf = SVR(kernel='linear', gamma='auto')
    clf.fit(features_train, labels_train)
    score = clf.score(features_test, labels_test)
    print('Result for linear svr is ' + str(score))

    clf = SVR(kernel='rbf', gamma='auto')
    clf.fit(features_train, labels_train)
    score = clf.score(features_test, labels_test)
    print('Result for rbf svr is ' + str(score))

    clf = SVR(kernel='poly', gamma='auto')
    clf.fit(features_train, labels_train)
    score = clf.score(features_test, labels_test)
    print('Result for polynomial svr is ' + str(score))

    clf = SVR(kernel='sigmoid', gamma='auto')
    clf.fit(features_train, labels_train)
    score = clf.score(features_test, labels_test)
    print('Result for sigmoid svr is ' + str(score))

def clustering_prediction(features, labels):
    features_train, features_test, labels_train, labels_test = train_test_split(
            features, labels, test_size=0.2, random_state=42)

    kmeans = KMeans(n_clusters=6, random_state=0)
    kmeans.fit(features_train)

    clusters = kmeans.labels_
    cluster_sum = dict()
    cluster_size = dict()
    cluster_mean = dict()

    for index in range(len(clusters)):
        center = clusters[index]
        if not (center in cluster_sum):
            cluster_sum[center] = 0.0
            cluster_size[center] = 0

        cluster_sum[center] = cluster_sum[center] + labels_train.iloc[index]
        cluster_size[center] = cluster_size[center] + 1

    for center in cluster_sum:
        cluster_mean[center] = float(cluster_sum[center]) / cluster_size[center]
    
    predicted_labels = kmeans.predict(features_test)
    predicted_labels = list(map(lambda center : cluster_mean[center], predicted_labels))

    score = mean_squared_error(labels_test.tolist(), predicted_labels)
    print('Kmeans clustering score is ' + str(score))

def multi_layer_perceptron(features, labels):
    features_train, features_test, labels_train, labels_test = train_test_split(
            features, labels, test_size=0.2, random_state=42)

    model = Sequential()
    # two hidden layers for neural network
    model.add(Dense(8, input_dim=features_train.shape[1], activation="relu"))
    model.add(Dense(4, activation="relu"))
    model.add(Dense(4, activation="relu"))
    model.add(Dense(1, activation="sigmoid"))
    model.summary()

    opt = Adam(lr=1e-3)
    model.compile(loss="mean_absolute_error", optimizer=opt)

    model.fit(features_train, labels_train, validation_data=(features_test, labels_test),
            epochs=1, batch_size=8)

    score = model.evaluate(features_test, labels_test, verbose=0)
    # returns scalar (only one output) test loss
    print('Mlp score is ' + str(score))

def main():
    rank_file = open('./rankings', 'r')
    rank_map = json.loads(rank_file.read().replace("\'", "\""))
    rank_file.close()

    #print(rank_map['t2_rlhim'])

    #pprint.pprint(rank_map)
    #pprint.pprint(split_by_range(rank_map))
    #pprint.pprint(split_by_position(rank_map))
    #pprint.pprint(log_ranks(rank_map))

    features = extract_columns('./regularity', 'user', ['PDH', 'PWD', 'WS1', 'WS2', 'WS3'])
    #features_individual_stats = extract_columns('./file.csv', 'user', ['feat1', 'feat2'])
    #features_textual_complexity = extract_columns('./file.csv', 'user', ['feat1', 'feat2'])
    #features = pd.concat([features_individual_stats, feature_textual_complexity])

    labels = create_labels_df(rank_map)
    svr_prediction(features, labels)
    clustering_prediction(features, labels)
    multi_layer_perceptron(features, labels)
    
    labels = create_labels_df(split_by_position(rank_map))
    svr_prediction(features, labels)
    clustering_prediction(features, labels)
    multi_layer_perceptron(features, labels)

    labels = create_labels_df(log_ranks(rank_map))
    svr_prediction(features, labels)
    clustering_prediction(features, labels)
    multi_layer_perceptron(features, labels)

    #print(features)
    #print(labels)
    #print(pd.concat([features, labels], axis=1))

    

if __name__ == '__main__':
    main()
