(ns conjurator.core
  (:require [play-clj.core :refer :all]
            [play-clj.ui :refer :all]
            [play-clj.g2d :refer :all]))

(declare update-player-position)

(def player-speed 10)

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
        :left (assoc entity :x (- old-x player-speed))
        :right (assoc entity :x (+ old-x player-speed))
        :up (assoc entity :y (+ old-y player-speed))
        :down (assoc entity :y (- old-y player-speed))))
    entity))

(defscreen main-screen
  :on-show
  (fn [screen entities]
    (update! screen :renderer (stage) :camera (orthographic))
    (let [gab-ganon (assoc (texture "gabganon.png") :x 100 :y 100 :player? true)
          background (assoc (texture "background.png") :width 800)]
      [background gab-ganon]))

  :on-resize
  (fn [screen entities]
    (height! screen 400)
    (width! screen 800))

  :on-render
  (fn [screen entities]
    (clear!)
    (render! screen entities))

  :on-key-down
  (fn [screen entities]
    (println (str "key down : " (:key screen)))
    (let [keycode (:key screen)
          direction (get-direction keycode)]
    (cond
      ;; Mouvement du personnage
      direction (move-player direction entities)))))

(defgame conjurator-game
  :on-create
  (fn [this]
    (set-screen! this main-screen)))

;; (app! :post-runnable #(set-screen! conjurator-game main-screen))
