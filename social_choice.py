import argparse
import copy
import csv
import os
import sys
import numpy as np
import pandas as pd


class InputError(Exception):

    def __init__(self, msg):
        self.msg = msg

    def __str__(self):
        return repr(self.msg)


class PreferenceSchedule():

    def __init__(self, candidates, prefs):
        # check whether the candidates list consists of only strings
        if not all(map(lambda x: type(x) == str, candidates)):
            raise InputError('Candidate must be a string')

        # check the validity of the preferences
        for pref in prefs:
            # check whether the number of candidates in the preference schedule
            # is valid
            if len(pref) != len(candidates):
                raise InputError('Invalid preference schedule')

            # check whether the candidates in the preference schedule are unique
            if len(pref) != len(candidates):
                raise InputError('Invalid preference schedule')

            # check whether the candidates in the preference schedule are also
            # in the candidates list
            for candidate in pref:
                if candidate not in candidates:
                    raise InputError('Invalid preference schedule')

        self.prefs = prefs

    def original(self):
        '''Returns the original preference schedule as a printable string'''

        res = ''
        for i in range(len(self.prefs)):
            res += 'Voter {}: '.format(i+1) + ', '.join(self.prefs[i]) + '\n'

        return res[:-1]

    def detailed(self):
        '''Returns the detailed preference schedule as a printable string'''

        # count the number of occurences of each preference
        prefs = self.prefs[:]
        prefs = [tuple(p) for p in self.prefs]
        counts = {}
        while prefs:
            pref = prefs.pop(0)
            count = 1
            while pref in prefs:
                prefs.remove(pref)
                count += 1
            counts[pref] = count

        res = ''
        for pref in counts:
            res += str(counts[pref]) + ' Voters: ' + ', '.join(pref) + '\n'

        return res[:-1]


class Aggregator():

    def __init__(self, file):
        df = pd.read_csv('input.csv', header=None)
        prefs = df.to_numpy()
        prefs[0] = prefs[0].astype(np.int)
        prefs = np.transpose(prefs)
        repeat = list(prefs[:, 0])
        prefs = np.delete(prefs, 0, axis=1)
        candidates = np.unique(np.sort(prefs[0], axis=None)).tolist()
        prefs = np.repeat(prefs, repeats=repeat, axis=0).tolist()
        self.candidates = candidates
        self.pref_schedule = PreferenceSchedule(candidates, prefs)

    def __str__(self):
        res = ''
        res += 'Preference Schedule:\n'
        res += self.pref_schedule.original() + '\n\n'
        res += 'Detailed Preference Schedule:\n'
        res += self.pref_schedule.detailed() + '\n'

        return res

    def plurality(self):
        '''Prints who wins by the plurality method'''

        counts = {}
        for pref in self.pref_schedule.prefs:
            highest = pref[0]
            if highest in counts:
                counts[highest] += 1
            else:
                counts[highest] = 1

        winner = []
        highest_votes = max(counts.values())
        for candidate in counts:
            if counts[candidate] == highest_votes:
                winner.append(candidate)

        print('The numbers of votes for each candidate:', counts)
        print('The Plurality winner(s) is(are)', find_winner(counts))

    def condorcet(self):
        '''Prints who wins by the Condorcet method'''

        points = {candidate: 0 for candidate in self.candidates}
        candidates = list(self.candidates)
        for candidate in candidates[:]:
            candidates.remove(candidate)
            for rival in candidates:
                candidate_points = 0
                for pref in self.pref_schedule.prefs:
                    if pref.index(candidate) < pref.index(rival):
                        candidate_points += 1
                    else:
                        candidate_points -= 1
                if candidate_points > 0:
                    points[candidate] += 1
                else:
                    points[rival] += 1

        print('The Condorcet winner(s) is(are)', find_winner(points))

    def borda(self):
        '''Prints who wins by the Borda count'''

        counts = {}
        candidates = list(self.pref_schedule.prefs[0])
        for candidate in candidates:
            counts[candidate] = 0

        max_point = len(candidates)
        for pref in self.pref_schedule.prefs:
            for i in range(len(pref)):
                counts[pref[i]] += max_point - i

        print('Borda scores:', counts)
        print('The Borda winner(s) is(are)', find_winner(counts))

    def copeland(self):
        '''Prints who wins by the Copeland’s Rule'''

        points = {candidate: 0 for candidate in self.candidates}
        candidates = list(self.candidates)
        for candidate in candidates[:]:
            candidates.remove(candidate)
            for rival in candidates:
                candidate_points = 0
                for pref in self.pref_schedule.prefs:
                    if pref.index(candidate) < pref.index(rival):
                        candidate_points += 1
                    else:
                        candidate_points -= 1
                if candidate_points > 0:
                    points[candidate] += 1
                else:
                    points[rival] += 1

        print('Copeland points:', points)
        print('The Copeland winner(s) is(are)', find_winner(points))

    def runoff(self):
        '''Prints who wins by the Instant runoff'''

        num_round = 1
        candidates = self.candidates[:]
        prefs = copy.deepcopy(self.pref_schedule.prefs)

        while len(candidates) >= 2:
            counts = {}
            for pref in prefs:
                highest = pref[0]
                if highest in counts:
                    counts[highest] += 1
                else:
                    counts[highest] = 1
            print('The numbers of votes for each candidate (round {}):'.format(
                num_round), counts)

            lowest_votes = min(counts.values())
            for candidate in counts:
                if counts[candidate] == lowest_votes:
                    candidates.remove(candidate)
                    for pref in prefs:
                        pref.remove(candidate)

            num_round += 1

        print('The Runoff winner(s) is(are)', find_winner(counts))


def find_winner(aggregated_result):
    max_point = 0
    for point in aggregated_result.values():
        if point > max_point:
            max_point = point

    winner = []  # winner can be many, so use a list here
    for candidate in aggregated_result.keys():
        if aggregated_result[candidate] == max_point:
            winner.append(candidate)

    return winner


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument(
        'csv', help='a csv file containing preferences', type=str)
    parser.add_argument('-m', '--method', type=str,
                        help='specify a winner selection method by a name')
    args = parser.parse_args()

    aggr = Aggregator(args.csv)

    if args.method:
        method = args.method
        try:
            if method == 'plurality':
                print('Plurality method\n')
                print(aggr)
                aggr.plurality()
            elif method == 'condorcet':
                print('Condorcet method\n')
                print(aggr)
                aggr.condorcet()
            elif method == 'borda':
                print('Borda count\n')
                print(aggr)
                aggr.borda()
            elif method == 'copeland':
                print('Copeland’s Rule\n')
                print(aggr)
                aggr.copeland()
            elif method == 'runoff':
                print('Instant runoff method\n')
                print(aggr)
                aggr.runoff()
            else:
                raise InputError('Invalid method name')
        except InputError as e:
            print('Error:', e.msg)
            sys.exit()
    else:
        # examine all winner selection methods
        print(aggr)
        print('Plurality method:')
        aggr.plurality()
        print('\nCondorcet method:')
        aggr.condorcet()
        print('\nBorda count:')
        aggr.borda()
        print('\nCopeland’s Rule:')
        aggr.copeland()
        print('\nInstant runoff method:')
        aggr.runoff()
