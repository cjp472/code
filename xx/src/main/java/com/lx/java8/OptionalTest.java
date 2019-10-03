package com.lx.java8;//说明:

import java.util.Optional;

/**
 * 创建人:游林夕/2019/3/26 17 27
 */
public class OptionalTest {
    public static void main(String [] args){
        Optional< String > firstName = Optional.of( "Tom" );
        System.out.println( "First Name is set? " + firstName.isPresent() );
        System.out.println( "First Name: " + firstName.orElseGet( () -> "[none]" ) );
        System.out.println( firstName.map( s -> "Hey " + s + "!" ).orElse( "Hey Stranger!" ) );
        System.out.println();
    }
}
