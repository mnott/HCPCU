#!/bin/bash
#
# Rename one Eclipse project to another
#
if [[ "$1" == "" ]]; then
  echo Usage: $0 [-git] sourcename targetname to rename the app into a different project
  exit 1
fi

if [[ "$1" != "-git" ]]; then
  git="-path ./.git -prune -o"
else
  git=""
  shift
fi

# get some transformations of our strings
export alc=$(echo $1 | tr [A-Z] [a-z])
export auc=$(echo $1 | tr [a-z] [A-Z])
export blc=$(echo $2 | tr [A-Z] [a-z])
export buc=$(echo $2 | tr [a-z] [A-Z])

echo Renaming: $1 to $2, $alc to $blc, $auc to $buc

# rename all those strings in those text files
find . $git -type f -exec grep -Il . {} \; -exec perl -pi -e "s/$1/$2/g" {} \;
find . $git -type f -exec grep -Il . {} \; -exec perl -pi -e "s/$alc/$blc/g" {} \;
find . $git -type f -exec grep -Il . {} \; -exec perl -pi -e "s/$auc/$buc/g" {} \;

# not nice but rare...
find . $git -type d -name "*$1" -exec sh -c 'mv $1 `dirname $1`/'$2 _ "{}" \; 2>/dev/null
find . $git -type d -name "*$alc" -exec sh -c 'mv $1 `dirname $1`/'$blc _ "{}" \; 2>/dev/null
find . $git -type d -name "*$auc" -exec sh -c 'mv $1 `dirname $1`/'$buc _ "{}" \; 2>/dev/null

