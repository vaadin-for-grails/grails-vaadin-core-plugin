package demo

class Book {

    String title
    String author
    Date released
    boolean available
    int rating

    static constraints = {
        title widget: 'textArea'
    }
}