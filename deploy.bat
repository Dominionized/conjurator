@echo off
hugo
git add -A
git commit
git subtree push --prefix public origin gh-pages