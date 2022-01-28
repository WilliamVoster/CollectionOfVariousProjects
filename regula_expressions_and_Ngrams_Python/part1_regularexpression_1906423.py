# student name : Thor William Voster
# studen prID  : 1906423

import re

#task 1.1 about finding currencies and money from a bbc article
def run_part_1_1():
    filename = "./bbc_article.txt"
    file = open(filename, "r")

    valutaInFront = "[£$€]"
    valutaBehind = "\s(?:penny|pence|pounds|pound|cents|cent|dollars|dollar|euros|euro)"
    numberPart = "(?:\d+,)*\d+(?:\.\d+)?(?:m|bn)?" #million, billion
    currencyAndQuantifier = "(?:p|c)\W" #pence, cent

    exp = "(" + \
        valutaInFront   + numberPart    + "|" + \
        numberPart      + valutaBehind  + "|" + \
        valutaInFront   + numberPart    + valutaBehind + "|" + \
        numberPart      + currencyAndQuantifier  + ")"

    while True:
        line = file.readline()

        if line == "": break
        
        hit = re.findall(exp, line)

        for number in hit:
            print(" -- ", number, "\t\t", line)

    file.close()

    #! Full regular expression:
    '''
    ([£$€](?:\d+,)*\d+(?:\.\d+)?(?:m|bn)?|(?:\d+,)*\d+(?:\.\d+)?(?:m|bn)?\s
    (?:penny|pence|pounds|pound|cents|cent|dollars|dollar|euros|euro)|[£$€]
    (?:\d+,)*\d+(?:\.\d+)?(?:m|bn)?\s(?:penny|pence|pounds|pound|cents|cent|dollars|dollar|euros|euro)|
    (?:\d+,)*\d+(?:\.\d+)?(?:m|bn)?(?:p|c)\W)
    '''

    #! Output/result from matching expression:
    '''
    --  $131bn              The Commerce Department estimated that storm-related damage to fixed assets, such as homes and government buildings, totalled more than $131bn (Â£100bn).

    --  £100bn              The Commerce Department estimated that storm-related damage to fixed assets, such as homes and government buildings, totalled more than $131bn (Â£100bn).

    --  $100bn              It also said it expected the government and insurers to pay more than $100bn in insurance claims, with foreign companies accounting for more than $17.4bn.

    --  $17.4bn             It also said it expected the government and insurers to pay more than $100bn in insurance claims, with foreign companies accounting for more than $17.4bn.

    #! checked for numbers in task 1.1 in the assignment document:
    --  £50,000             currencies, for example Â£50,000 and Â£117.3m as well as 30p, 500m euro,

    --  £117.3m             currencies, for example Â£50,000 and Â£117.3m as well as 30p, 500m euro,

    --  30p,                currencies, for example Â£50,000 and Â£117.3m as well as 30p, 500m euro,

    --  500m euro           currencies, for example Â£50,000 and Â£117.3m as well as 30p, 500m euro,

    --  338bn euros         338bn euros, $15bn and $92.88. Make sure that you can at least detect

    --  $15bn               338bn euros, $15bn and $92.88. Make sure that you can at least detect

    --  $92.88              338bn euros, $15bn and $92.88. Make sure that you can at least detect
    '''


#task 1.2 about matching phone numbers
def run_part_1_2():
    filename = "./phone_numbers.txt"
    file = open(filename, "r")

    exp = "(?:\+?\d-)?\(?\d{3}\)?(?:[ .-]?\d{3})?[ .-]?\d{4}\d?"

    i=0
    while True:
        line = file.readline()

        if line == "": break
        
        hit = re.findall(exp, line)

        for number in hit:
            print(" -- ", number, "\t\t", line)
            i += 1

    file.close()
    print(i, "/14 numbers found")


#if __name__ == "__main__": #! you might run this from a script and not as main
    
print("\n\nTASK 1")
run_part_1_1()

print("\n\nTASK 2")
run_part_1_2()