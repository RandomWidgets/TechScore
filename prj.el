; (jde-project-file-version "1.0")
(jde-set-variables
 ;; Set here default parameter for make program
 '(jde-make-args "jar")
 ;; What should be put as java file header
;;  '(jde-gen-buffer-boilerplate 
;;    (quote 
;;     ("/*" 
;;      " * Geotools - OpenSource mapping toolkit" 
;;      " * http://geotools.org" 
;;      " * (C) 2002, Geotools Project Managment Committee (PMC)" 
;;      " *" 
;;      " * This library is free software; you can redistribute it and/or" 
;;      " * modify it under the terms of the GNU Lesser General Public" 
;;      " * License as published by the Free Software Foundation;" 
;;      " * version 2.1 of the License." 
;;      " *" 
;;      " * This library is distributed in the hope that it will be useful," 
;;      " * but WITHOUT ANY WARRANTY; without even the implied warranty of" 
;;      " * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU" 
;;      " * Lesser General Public License for more details." 
;;      " *" 
;;      " */")))
 ;; Sometimes JDEE prints useful messages, but if everything works well 
 ;; you will be not using this.
 '(jde-log-max 5000)
 ;; Must be on to improve your coding: you write: "if " and JDEE generates
 ;; templeate code for "if" statement.
 '(jde-enable-abbrev-mode t)
;;  ;; Path to source files for automatic loading
;;  '(jde-sourcepath 
;;    (quote 
;;     ("$PROJECTS_HOME/geotools2/geotools-src/vpf/src/" 
;;      "$PROJECTS_HOME/geotools2/geotools-src/core/src/" 
;;      "$PROJECTS_HOME/geotools2/geotools-src/defaultcore/src/" 
;;      "$PROJECTS_HOME/geotools2/geotools-src/shapefile/src/" 
;;      "$PROJECTS_HOME/geotools2/geotools-src/utils/src/" 
;;      "$JAVA_HOME/src/")))
;;  ;; Classpath for browsing files and generates code templates
;;  '(jde-global-classpath 
;;    (quote 
;;     ("$PROJECTS_HOME/geotools2/geotools-src/vpf/src/" 
;;      "$PROJECTS_HOME/geotools2/geotools-src/vpf/target/classes/" 
;;      "$PROJECTS_HOME/geotools2/geotools-src/core/target/classes/" 
;;      "$PROJECTS_HOME/geotools2/geotools-src/defaultcore/target/classes/" 
;;      "$PROJECTS_HOME/geotools2/geotools-src/shapefile/target/classes/" 
;;      "$PROJECTS_HOME/geotools2/geotools-src/utils/target/classes/" 
;;      "$MAVEN_HOME/repository/JTS/jars/JTS-1.2.jar")))
 ;; If you want to run Java apps from within emacs for example for debuging
 ;; set default startup class for your project.
 '(jde-run-application-class "tscore.TScoreGUI")
 '(jde-run-working-directory "/home/dayan")
 ;; Set name for your make program: ant or maybe maven?
 '(jde-make-program "ant")
 ;; For javadoc templates version tag can be customized 
 '(jde-javadoc-version-tag-template "\"* @version $Id: prj.el,v 1.4 2003/04/23 14:28:25 kobit Exp $\"")
 ;; Defines bracket placement style - now it is set according to SUN standards
 '(jde-gen-k&r t)
 ;; Do you prefer to have java.io.* imports or separate import for each 
 ;; used class - now it is set for importing classes separately
 '(jde-import-auto-collapse-imports nil)
;; ;; You can define many JDKs and choose one for each project
;; '(jde-compile-option-target (quote ("1.4")))
 ;; Nice feature sorting imports.
 '(jde-import-auto-sort t)
 ;; For syntax highlighting and basic syntax checking parse buffer
 ;; number of seconds from the time you changed the buffer.
 '(jde-auto-parse-buffer-interval 600)
;; ;; Only for CygWin users it improves path resolving
;; '(jde-cygwin-path-converter (quote (jde-cygwin-path-converter-cygpath)))
 ;; You can set different user name and e-mail address for each project
 '(user-mail-address "dpv140@users.sourceforge.net")
)
