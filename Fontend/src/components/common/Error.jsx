export default function Error({ message }) {
    return (
        <div className="flex items-center justify-center h-96">
            <p className="text-red-500">{message}</p>
        </div>
    );
}
