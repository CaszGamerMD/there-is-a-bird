package EdDYON.guaniao.client.camera;

/** Standalone Java 25 test of original shutter arming during raw mouse input. */
public final class GuaniaoCameraShutterInputRegression {
    private GuaniaoCameraShutterInputRegression() {}

    private static void verify(boolean condition, String caseName) {
        if (!condition) throw new AssertionError(caseName);
    }

    public static void main(String[] args) {
        CameraShutterInput input = new CameraShutterInput();
        input.reset();
        verify(!input.press(0), "Opening click cannot fire shutter");
        input.release(0);
        input.observe(true, false, false);
        verify(input.isArmed(), "Both buttons released arms shutter");
        verify(input.press(0), "First fresh left press captures");
        verify(!input.press(0), "Held left button cannot capture twice");
        input.release(0);
        input.observe(true, false, false);
        verify(input.press(1), "Fresh right press closes camera");
        input.release(1);

        input.reset();
        input.observe(true, false, false);
        input.suspend(true);
        input.observe(true, false, false);
        verify(!input.isArmed(), "Keyboard shortcut must suspend shutter");
        verify(!input.press(0), "Shortcut-return click cannot capture");
        input.release(0);
        input.observe(true, false, false);
        verify(input.isArmed(), "Next fully released click can capture");
        verify(input.press(0), "Fresh post-shortcut click captures");

        input.reset();
        input.observe(true, false, false);
        input.suspend(false);
        verify(!input.isArmed(), "External screen suspends immediately");
        input.observe(false, false, false);
        verify(!input.isArmed(), "Unfocused game stays disarmed");
        input.observe(true, false, false);
        verify(input.isArmed(), "Focus recovery needs release");
        input.recaptured();
        verify(!input.isArmed(), "Mouse regrab must not fire shutter");
        input.observe(true, false, false);
        verify(input.isArmed(), "After regrab release shutter arms again");

        input.reset();
        input.observe(true, false, false);
        verify(input.press(0), "Final fresh press accepted");
        input.release(0);
        input.observe(true, true, false);
        verify(!input.isArmed(), "Still-held button cannot rearm");

        System.out.println("PASS: original shutter fresh press, release, shortcuts and focus");
    }
}
