package antimonypidgey.untangle;

// Thread class which handles the timer.
public class TimerContainer{
    private long startTime = 0;
    private boolean running = false;
        private long currentTime = 0;

        public void start() {
            startTime = System.currentTimeMillis();
            running = true;
        }

        public void stop() {
            running = false;
        }

        public void pause() {
            running = false;
            currentTime = System.currentTimeMillis() - startTime;
        }
        public void resume() {
            running = true;
            startTime = System.currentTimeMillis() - currentTime;
        }

        public boolean isRunning(){
            return running;
        }

        //elaspsed time in milliseconds
        public long getElapsedMS() {
            long elapsed = 0;
            if (running) {
                elapsed =((System.currentTimeMillis() - startTime));
            }
            else elapsed = currentTime;
            return elapsed;
        }

        // String representing timer
        public String getElapsed(){
            long milliSeconds = getElapsedMS();
            long millis = Math.round((milliSeconds % 1000)/100);
            long seconds = (milliSeconds / 1000) % 60;
            long minutes = (milliSeconds / (1000 * 60)) % 60;
            long hours= (milliSeconds / (1000 * 60 * 60)) % 24;
            if (hours>0)
                return String.format("%02d:%02d:%02d:%02d", hours, minutes, seconds, millis);
            else
                return String.format("%02d:%02d:%02d", minutes, seconds, millis);
        }
}
