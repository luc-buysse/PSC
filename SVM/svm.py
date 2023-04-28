import sys
import numpy as np
import pandas as pd
import sklearn as sk
import sklearn.svm as svm
import sklearn.datasets
import os
import seaborn as sns
from datetime import datetime
import matplotlib.pyplot as plt


if __name__=="__main__":

    os.chdir("/home/luc/Desktop/Completer")

    dataset_train = pd.read_csv("stats_psc.csv", sep=";")

    X_unscaled = dataset_train.iloc[1:,6:90]
    y = dataset_train.iloc[1:,4]

    scaler = sk.preprocessing.StandardScaler().fit(X_unscaled)
    X = scaler.transform(X_unscaled)

    y = [1 if type == "A" else -1 for type in y]

    clf = svm.SVC(kernel='linear')
    clf.fit(X, y)

    coef_normalized = clf.coef_ / np.sum(clf.coef_)

    weights = pd.DataFrame(np.array(coef_normalized), columns = dataset_train.iloc[0,6:90])

    weights.to_csv("computed_weights.csv")

    names = np.array(dataset_train.iloc[1:33,0])
    distances =[(np.dot(clf.coef_, x)[0] + clf.intercept_[0]) for x in X];

    distances_to_plot = [(distances[i], names[i]) for i in range(len(distances))]
    distances_to_plot.sort()

    for i in range(len(distances_to_plot)):
        distances[i] = distances_to_plot[i][0]
        names[i] = distances_to_plot[i][1]

    distances_labeled = pd.DataFrame(np.array([distances]), columns = dataset_train.iloc[1:33,0])
    distances_labeled.to_csv("linear_classification.csv")

    #Point scattering on a line
    levels = [(i % 10 + 1) * 0.1 for i in range(len(distances))]

    fig, ax = plt.subplots(figsize=(8.8, 4), layout="constrained")
    ax.set(title="Position par rapport à l'hyperplan (en 0)")

    ax.vlines(distances, 0, levels, color="tab:grey", linestyle="--")  # The vertical stems.
    ax.plot(distances, np.zeros_like(distances), "-o",
            color="k", markerfacecolor="w")  # Baseline and markers on it.

    # annotate lines
    for d, l, r in zip(distances, levels, names):
        ax.annotate(r, xy=(d, l),
                    xytext=(-3, -np.sign(l)*12), textcoords="offset points",
                    horizontalalignment="right",
                    verticalalignment="bottom" if l > 0 else "top")

    # format x-axis with 4-month intervals
    plt.setp(ax.get_xticklabels(), rotation=0, ha="right")

    # remove y-axis and spines
    ax.yaxis.set_visible(False)
    ax.spines[["left", "top", "right"]].set_visible(False)

    ax.margins(y=0.1)
    plt.show()
