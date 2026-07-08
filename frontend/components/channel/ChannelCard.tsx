import type { Channel } from "@/types";
import styles from "./ChannelCard.module.css";

export default function ChannelCard({ channel }: { channel: Channel }) {
  return (
    <article className={styles.card}>
      <h4 className={styles.name}>{channel.name}</h4>
      <p className={styles.description}>{channel.description}</p>
    </article>
  );
}
