fun main(args: Array<String>) {
    var a: Int = 1;
    var b: Int = 1;
    var temp: Int = 0;

    while (a < 144) {
        temp = b;
        b = a + b;
        a = temp;
        println(a);
    }
}