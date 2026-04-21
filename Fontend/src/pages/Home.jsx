import Navbar from "../components/layout/Navbar";
import Hero from "../components/home/Hero";
import SearchBar from "../components/home/SearchBar";
import MovieSection from "../components/home/MovieSection";
import VipPromo from "../components/common/VipPromo";
import Footer from "../components/layout/Footer";

export default function Home() {
    return (
        <div>
            <Navbar />
            <Hero />
            <SearchBar />
            <MovieSection />
            <VipPromo />
            <Footer />
        </div>
    );
}