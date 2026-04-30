import Navbar from "../components/layout/Navbar";
import Hero from "../components/home/Hero";
import SearchBar from "../components/home/SearchBar";
import MovieSection from "../components/movies/MovieSection";
import Footer from "../components/layout/Footer";

export default function Home() {
    return (
        <div>
            <Navbar />
            <Hero />
            <SearchBar />
            <MovieSection />
            <Footer />
        </div>
    );
}