import sys
import numpy as np
import pandas as pd
import sklearn as sk
import sklearn.svm as svm
import sklearn.datasets
import os


if __name__=="__main__":

    os.chdir("/home/luc/Desktop/Completer")

    dataset_train = pd.read_csv("stats_psc.csv",
                                sep=";",
                                index_col=0,
                                skiprows = 0,  # skip comments describing scooter dataset
                                header=None)  # no name of columns

    X_unscaled = dataset_train.iloc[1:,5:90]
    y = dataset_train.iloc[1:,3]

    scaler = sk.preprocessing.StandardScaler().fit(X_unscaled)
    X = scaler.transform(X_unscaled)

    y = [1 if type == "A" else -1 for type in y]

    clf = svm.LinearSVC()
    clf.fit(X, y)

    coef_normalized = clf.coef_ / np.sum(clf.coef_)

    weights = pd.DataFrame(np.array(coef_normalized), columns = dataset_train.iloc[0,5:90])

    weights.to_csv("computed_weights.csv")

