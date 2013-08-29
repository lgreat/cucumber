


1) Install ruby: http://www.ruby-lang.org/en/
  a. Windows: http://rubyinstaller.org/downloads/
2) Install git: http://git-scm.com/
  a. On windows be sure to set core.autocrlf to input (middle radio button on one of the installer pages)
3) Configure git.
  a. One way is to follow http://wiki.greatschools.net/bin/view/Greatschools/UsingGit
  b. Another is to copy a known .gitconfig file into your home dir (see Dave for this option)
4) Clone the GSWeb repo which contains the cucumber tests, if you don't already have a local copy:
  'git clone git@githost.greatschools.org:GSWeb'
5) Install the ruby Bundler gem on your system: 'gem install bundler'
6) Update project gems: Go to the cucumber directory, and run 'bundle install'
7) Run cucumber in root of checkout: 'cucumber'


** Update the gems on your system:

View if you have the latest Gems on you system:

'gem query --local'

To update all gems - 
On >Ruby start Command prompt type:  

'gem update'
