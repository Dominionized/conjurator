(ns conjurator.utils
  (:require [play-clj.core :refer :all]))

(def player-speed 10)
(def max-player-speed 7)
(def ground-resistance 0.5)
(def accel 2)
(def jump-accel 5)
(def gravity-accel -0.1)

(def fps-counter-color (color :black))

;; Keys
(def jump-key :x)
