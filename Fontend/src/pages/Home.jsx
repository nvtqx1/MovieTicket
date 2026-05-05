import Hero from "../components/home/Hero";
import SearchBar from "../components/home/SearchBar";
import MovieSection from "../components/movies/MovieSection";
import Footer from "../components/layout/Footer";
import { useMovies } from "../hooks/useMovies";

export default function Home() {
    const { movies, loading, error } = useMovies();
    const featured = movies[0]; // Phim đầu tiên làm banner

    return (
        <div>
            <Hero movie={featured} loading={loading} />
            <SearchBar />
            <MovieSection movies={movies} loading={loading} error={error} />
            <Footer />
        </div>
    );
}
