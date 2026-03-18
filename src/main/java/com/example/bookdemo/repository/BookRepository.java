package com.example.bookdemo.repository;

import com.example.bookdemo.model.Book;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class BookRepository {

    private final List<Book> books = new ArrayList<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    public synchronized Book save(String title, String author) {
        Book book = new Book(idSequence.getAndIncrement(), title, author);
        books.add(book);
        return book;
    }

    public synchronized List<Book> findAll() {
        return new ArrayList<>(books);
    }

    public synchronized void clear() {
        books.clear();
        idSequence.set(1);
    }
}
