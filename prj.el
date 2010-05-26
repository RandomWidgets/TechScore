(jde-project-file-version "1.4")
(setq cwd (file-name-directory (expand-file-name "/home/dayan/techscore-dev/")))
(jde-set-variables
 '(jde-log-max 5000)
 '(jde-enable-abbrev-mode t)
 '(jde-make-program "ant")
 '(jde-run-application-class "edu.mit.techscore.tscore.TScoreGUI")
 
 ;; hack to make this directory the working directory
 '(jde-make-working-directory cwd)
 '(jde-sourcepath (concat cwd "src")))
(jde-set-global-classpath (concat cwd "bin"))
(jde-set-compile-options  (concat "-d " cwd "bin"))
