package deque;

public class ArrayDeque<T> implements Deque<T> {
    private T[] items;
    private int size;
    private int rear;
    private int front;

    public ArrayDeque() {
        items = (T[]) new Object[100];
        size = 0;
        front = 0;
        rear = 0;
    }
    public ArrayDeque(int capacity) {
        items = (T[]) new Object[capacity];
        size = 0;
        front = 0;
        rear = 0;
    }

    public void addFirst(T item){
        if (size == items.length ) resize();
        front = (front - 1 + items.length) % items.length;
        items[front] = item;
        size++;
    }
    public void addLast(T item) {
        if (size == items.length ) resize();
        items[rear] = item;
        rear = (rear + 1) % items.length;
        size++;
    }
    public boolean isEmpty() {
        return size == 0;
    }
    public int size() {
        return size;
    }
    public void printDeque() {
        // rear front && size == length works same as rear front not full
        // size == 0
        // front size situation
        if (rear < front & size != 0) { // length: 5 front:4 rear:2 so it's [4, 0, 1]
            int f = front;
            while(f < size){
                System.out.print(items[f]+" ");
                f++;
            }
            for(int i = 0; i < rear; i++) {
                System.out.print(items[i]+ " ");
            }
        } else if (size == 0) {
            return;
        }
        if (front < rear) {
            int i = front;
           while (i < rear) {
               System.out.print(items[i]+" ");
               i++;
           }
        }
        if (rear == front && size == items.length) {

        }
        System.out.println();  // Print a new line after the deque is printed
    }

    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        T element = items[front];
        front = ( front + 1 ) % items.length;
        size--;
        return element;
    }
    public T removeLast() {
        rear = ( rear - 1 + items.length ) % items.length;
        T element = items[rear];
        size--;
        return element;
    }
    public T get(int index) {
        if (index >= size && index < 0) {
            return null;
        }
        int trueindex = (front + index) % items.length;
        return items[trueindex];

    }
    public void resize() {
        T[] Nitems = (T[])new Object[items.length*2];
        System.arraycopy(items,0,Nitems,0,items.length);
        items = Nitems;
        front = 0;
        rear = size;
    }

//    public Iterator<T> iterator(){}
//    public boolean equals(Object o){}
}


