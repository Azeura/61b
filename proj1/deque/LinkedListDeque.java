package deque;

public class LinkedListDeque<T> implements Deque<T> {
    Node sentinal;
    int size;

    public class Node {
        T value;
        Node pre;
        Node next;

        public Node(T x, Node p, Node n) {
            this.value = x;
            this.pre = p;
            this.next = n;
        }
        public Node() {
            this(null, null, null); // Default value and null links
        }

    }

    public LinkedListDeque() {
//        Node lnl = new Node(item, this.sentinal, this.sentinal);
//        this.sentinal.next = lnl;
//        this.sentinal.pre = lnl;
//        size = 1;
        sentinal = new Node();
        sentinal.next = sentinal;
        sentinal.pre = sentinal;
    }


    public void addFirst(T item) {
        this.sentinal.next = new Node(item, this.sentinal,  this.sentinal.next);
        this.sentinal.next.next.pre = this.sentinal.next;
        size ++;
    }

    public T removeFirst() {
        Node p = this.sentinal.next;
        this.sentinal.next.next.pre = this.sentinal;
        this.sentinal.next = this.sentinal.next.next;
        size --;
        return p.value;
    }

    public void addLast(T item) {
        Node p = new Node(item, this.sentinal.pre , this.sentinal );
        this.sentinal.pre.next = p;
        this.sentinal.pre = p;

        size ++;
    }

    public T removeLast() {
        Node p = this.sentinal.pre;
        this.sentinal.pre = this.sentinal.pre.pre;
        this.sentinal.pre.next = this.sentinal;
        size --;
        return p.value;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        if (  size == 0 ) {
            return true;
        }
        return false;
    }

    public T get (int index) {
        if (index > this.size - 1 || this.size <= 0) {
            return null;
        }
        Node p = this.sentinal;
        while ( index >= 0) {
            p = p.next;
            index--;
        }
        return p.value;
    }

    public void printDeque() {
        Node p = this.sentinal.next; // Start from the first element
        while (p != sentinal) { // Stop when we reach the sentinel node
            System.out.print(p.value + " ");
            p = p.next;
        }
        System.out.println();
    }




}
