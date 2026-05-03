import { useNavigate } from "react-router-dom";

export default function MovieCard({ movie }) {
    const navigate = useNavigate();
    const id = movie.id || movie._id;
    const posterSrc = movie.posterImageUrl || movie.posterUrl || movie.image || "/fallback.svg";

    return (
        <div className="group cursor-pointer flex flex-col">
            <div className="relative aspect-[2/3] rounded-xl overflow-hidden bg-gray-800">
                <img
                    src={posterSrc}
                    alt={movie.title}
                    loading="lazy"
                    onError={(e) => {
                        e.target.onerror = null;
                        e.target.src = "/fallback.svg";
                    }}
                    className="block w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
                />

                <div className="absolute inset-0 bg-black/60 opacity-0 group-hover:opacity-100 flex flex-col justify-end p-4 transition-opacity duration-300">
                    <button
                        onClick={() => navigate(`/booking/${id}`)}
                        className="bg-red-600 hover:bg-red-700 text-white font-bold py-2 mb-2 rounded"
                    >
                        ĐẶT
                    </button>

                    <button
                        onClick={() => navigate(`/movies/${id}`)}
                        className="bg-white/20 hover:bg-white/30 text-white font-bold py-2 rounded backdrop-blur-sm"
                    >
                        CHI TIẾT
                    </button>
                </div>
            </div>

            <h3 className="mt-3 font-bold text-lg text-white truncate">
                {movie.title}
            </h3>
            <p className="text-sm text-gray-400">{movie.genre}</p>
        </div>
    );
}
