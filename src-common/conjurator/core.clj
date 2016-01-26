(ns conjurator.core
  (:require [play-clj.core :refer :all]
            [play-clj.ui :refer :all]
            [play-clj.g2d :refer :all]
            [play-clj.core :refer :all]
            [conjurator.utils :as u]))

(use 'clojure.pprint)

(declare update-player-position update-physics)
(defn- get-direction [keycode]
  (cond
    (= keycode (key-code :dpad-up)) :up
    (= keycode (key-code :dpad-down)) :down
    (= keycode (key-code :dpad-left)) :left
    (= keycode (key-code :dpad-right)) :right))

(defn- move-player [direction entities]
  (map #(update-player-position direction %) entities))

(defn- update-player-position [direction entity]
  (if (:player? entity)
    (let [old-x (:x entity)
          old-y (:y entity)]
      (case direction
        :left (assoc entity :x (- old-x u/player-speed))
        :right (assoc entity :x (+ old-x u/player-speed))
        :up (assoc entity :y (+ old-y u/player-speed))
        :down (assoc entity :y (- old-y u/player-speed))))
    entity))

(defn- update-physics [entity]
  (if (:player? entity)
      (assoc entity :y (- (:y entity) 1))
    entity))

(defn- update-fps-counter [entities]
  (map (fn [{fps? :fps? :as ent}]
         (if fps?
           (doto ent (label! set-text (str (game :fps))))
           ent))
       entities))

(defscreen main-screen
  :on-show
  (fn [screen entities]
    (println (+ 2 2))
    (update! screen :renderer (stage) :camera (orthographic))
    (let [gab-ganon (assoc (texture "gabganon.png") :x 100 :y 100 :player? true)
          background (assoc (texture "background.png") :width 800 :background? true)
          fps-counter (assoc (label "0" u/fps-counter-color) :fps? true)]
      [background gab-ganon fps-counter]))

  :on-resize
  (fn [screen entities]
    (height! screen 400)
    (width! screen 800))

  :on-render
  (fn [screen entities]
    (clear!)
    (println (game :fps))
    (->> entities
         (map update-physics)
         (update-fps-counter)
         (render! screen)))

  :on-key-down
  (fn [screen entities]
    (let [keycode (:key screen)
          direction (get-direction keycode)]
    (cond
      ;; Mouvement du personnage
      direction (move-player direction entities)))))

:on-key-up

(defgame conjurator-game
  :on-create
  (fn [this]
    (music "music/YoshiTheme.mp3" :play)
    (set-screen! this main-screen)))

;; (app! :post-runnable #(set-screen! conjurator-game main-screen))
