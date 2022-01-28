# student name : Thor William Voster
# studen prID  : 1906423

import re
from typing import Sequence
from nltk.book import text7 as text
from nltk import FreqDist

from nltk.corpus import brown
from nltk.util import ngrams


tokens = []
for word in text.tokens:

    word = word.lower()

    match = re.search(r'(^[a-z]+)\'?([a-z]*)', word) 
    #^^ words like "haven't" are split into 2 groups: 'haven' and 't' ==> later combined

    if match == None: continue

    #tokens.append(word[:match.end()])
    tokens.append(match.group(1) + match.group(2))

distribution = FreqDist(tokens)

print("\n\n50 highest frequency words in text7:\n", distribution.most_common(50))


#! Language modelling part
print("\n\nLanguage modelling part: ")

brown_words = [word.lower() for word in brown.words() if re.search("\w", word) or re.search("\.", word)]
total_n_words = len(brown_words)
total_n_trigrams = -3**2 + (total_n_words + 1) * 3 #from formula calculating number of n-grams
print('\nTotal words in corpus: {} \t Total trigrams made: {}\n'.format(total_n_words, total_n_trigrams))


def prob_next_word(ngram_dist, n, given_sequence, log=True):
    count_given_sequence = 0
    ngrams_given_sequence = []

    for ngram in ngram_dist.keys():
        if ngram[:n-1] == given_sequence:
            ocurrences = ngram_dist.get(ngram)
            count_given_sequence += ocurrences
            #print(ngram, ocurrences)

            ngrams_given_sequence.append((ngram, ocurrences))

    sorted_ngrams = sorted(ngrams_given_sequence, key = lambda x: x[1], reverse = True)

    if log: 
        print('Gven the sequence: {} - the probability of the next word is the following for this corpus:'.format(given_sequence))
        for e in sorted_ngrams:
            next_word = e[0][n-1]
            chance_of_word = e[1] / count_given_sequence

            print('{:<15}: {:.2f}%'.format(next_word, chance_of_word * 100))

    
    return sorted_ngrams[0][0][n-1]

trigram = ngrams(brown_words, 3)
trigram_dist = FreqDist(trigram)

prob_next_word(trigram_dist, 3, ('i', 'am'))


print("\n\nGenerating a sentence\n")

sequence = ('i', 'am')
sentence = "I am"

i=0
while i<100: # 100 max length
    i+=1

    next_word = prob_next_word(trigram_dist, 3, sequence, False)

    sequence = sequence[1], next_word
    sentence += " " + next_word

    if next_word == '.': break

print("Demo sentence generated with most probable word:\n", sentence)


sents = brown.sents()
for sent in sents:
    if sent[0].lower() == "i" and sent[1].lower() == "am":
        print("Demo sentence searched:\n", sent)
        break