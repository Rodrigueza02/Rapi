import { ArrowRight, LoaderCircle } from 'lucide-react';

export default function LoginButton({ loading }) {
  return (
    <button className="primary-button" type="submit" disabled={loading}>
      {loading
        ? <LoaderCircle className="spin" size={20} />
        : <> Iniciar Sesión <ArrowRight size={20} /> </>
      }
    </button>
  );
}
